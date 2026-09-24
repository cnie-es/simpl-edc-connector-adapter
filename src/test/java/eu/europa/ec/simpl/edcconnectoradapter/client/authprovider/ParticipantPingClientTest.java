package eu.europa.ec.simpl.edcconnectoradapter.client.authprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import eu.europa.ec.simpl.edcconnectoradapter.client.authprovider.ParticipantPingClient.ParticipantIdentity;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Covers the caching policy of the identity resolution: a resolved identity is reused, while a failed resolution is
 * only remembered for the (short) negative TTL and is then retried.
 */
class ParticipantPingClientTest {

    private static final String PING_PATH = "/tier1/v2/ping";

    private static final String SELF_PATH = "/tier1/v2/participant";

    private static final String PROVIDER_ENDPOINT = "https://tls-participant-cniestage.cnie.internal/edc";

    private static final String IDENTITY_JSON = "{\"id\":\"urn:uuid:provider-1\",\"organization\":\"CNIE-Stage\"}";

    private final List<String> receivedQueries = new ArrayList<>();

    /** Number of requests to answer with a 500 before starting to answer with the identity. */
    private final AtomicInteger failuresToServe = new AtomicInteger();

    private HttpServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(PING_PATH, this::handle);
        server.createContext(SELF_PATH, this::handle);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private void handle(HttpExchange exchange) throws IOException {
        receivedQueries.add(exchange.getRequestURI().getQuery());
        if (failuresToServe.getAndDecrement() > 0) {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
            return;
        }
        byte[] body = IDENTITY_JSON.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private ParticipantPingClient clientWithTtls(long ttlSeconds, long negativeTtlSeconds) {
        ParticipantPingClient client = new ParticipantPingClient();
        ReflectionTestUtils.setField(
                client, "baseUrl", "http://127.0.0.1:" + server.getAddress().getPort());
        ReflectionTestUtils.setField(client, "pingPath", PING_PATH);
        ReflectionTestUtils.setField(client, "selfPath", SELF_PATH);
        ReflectionTestUtils.setField(client, "cacheTtlSeconds", ttlSeconds);
        ReflectionTestUtils.setField(client, "negativeCacheTtlSeconds", negativeTtlSeconds);
        ReflectionTestUtils.invokeMethod(client, "init");
        return client;
    }

    @Test
    void resolveByEndpointRetriesOnceTheNegativeTtlExpired() {
        // a momentarily unavailable authentication-provider (e.g. a 500) must not blank the participant name for the
        // rest of the process lifetime: the next resolution attempt has to reach it again
        failuresToServe.set(1);
        ParticipantPingClient client = clientWithTtls(1800, 0);

        assertNull(client.resolveByEndpoint(PROVIDER_ENDPOINT), "a failed resolution must resolve to no identity");

        ParticipantIdentity identity = client.resolveByEndpoint(PROVIDER_ENDPOINT);
        assertNotNull(identity, "the failed resolution must be retried once the negative TTL expired");
        assertEquals("CNIE-Stage", identity.organization());
        assertEquals("urn:uuid:provider-1", identity.id());
        assertEquals(2, receivedQueries.size(), "the authentication-provider must have been pinged twice");
    }

    @Test
    void resolveByEndpointReusesTheResolvedIdentityWithinTheTtl() {
        ParticipantPingClient client = clientWithTtls(1800, 60);

        ParticipantIdentity first = client.resolveByEndpoint(PROVIDER_ENDPOINT);
        ParticipantIdentity second = client.resolveByEndpoint(PROVIDER_ENDPOINT);

        assertEquals(first, second);
        assertEquals(1, receivedQueries.size(), "a resolved identity must be served from the cache");
    }

    @Test
    void resolveByEndpointPingsTheFqdnInLowerCase() {
        ParticipantPingClient client = clientWithTtls(1800, 60);

        assertNotNull(client.resolveByEndpoint("https://TLS-Participant-CNIEStage.cnie.internal/edc"));

        assertEquals("fqdn=tls-participant-cniestage.cnie.internal", receivedQueries.get(0));
    }

    @Test
    void resolveOwnRetriesOnceTheNegativeTtlExpired() {
        failuresToServe.set(1);
        ParticipantPingClient client = clientWithTtls(1800, 0);

        assertNull(client.resolveOwn());

        assertNotNull(client.resolveOwn(), "a failed own-identity resolution must be retried");
        assertEquals(2, receivedQueries.size());
    }

    @Test
    void resolveByEndpointIsDisabledWithoutBaseUrl() {
        ParticipantPingClient client = new ParticipantPingClient();
        ReflectionTestUtils.setField(client, "baseUrl", "");
        ReflectionTestUtils.invokeMethod(client, "init");

        assertNull(client.resolveByEndpoint(PROVIDER_ENDPOINT));
        assertEquals(0, receivedQueries.size(), "no base url must mean no call at all");
    }
}
