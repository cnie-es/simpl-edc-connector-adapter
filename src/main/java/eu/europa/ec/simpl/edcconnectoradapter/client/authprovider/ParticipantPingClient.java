package eu.europa.ec.simpl.edcconnectoradapter.client.authprovider;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Resolves a counterparty's organization name from the authentication-provider {@code ping} endpoint, using the
 * connection endpoint (FQDN) already available in the transfer/negotiation request.
 *
 * <p>The authentication-provider, given the FQDN of a counterparty connector, returns its verified identity
 * ({@code id} + {@code organization}). This lets us label transfers with the participant name without depending on a
 * participant-registry lookup by id. Resolution is best-effort and cached by FQDN; when no base URL is configured the
 * client is disabled and returns {@code null}.
 *
 * <p>Both successful and failed resolutions are cached, but with different lifetimes: a participant identity is
 * stable, so a hit is kept for a long TTL, while a miss (the authentication-provider being momentarily unavailable,
 * e.g. a 500) is kept only for a short one. This keeps a transient outage from permanently blanking the participant
 * name on every subsequent record without hammering the authentication-provider while the outage lasts.
 */
@Log4j2
@Component
public class ParticipantPingClient {

    @Value("${participant.auth-api.base-url:}")
    private String baseUrl;

    @Value("${participant.auth-api.ping-path:/tier1/v2/ping}")
    private String pingPath;

    @Value("${participant.auth-api.self-path:/tier1/v2/participant}")
    private String selfPath;

    /** How long a resolved identity is reused before being resolved again. */
    @Value("${participant.auth-api.cache.ttl-seconds:1800}")
    private long cacheTtlSeconds;

    /** How long a failed resolution is remembered before being retried; deliberately short. */
    @Value("${participant.auth-api.cache.negative-ttl-seconds:60}")
    private long negativeCacheTtlSeconds;

    private RestClient restClient;
    private final Map<String, CachedIdentity> cache = new ConcurrentHashMap<>();
    private volatile CachedIdentity ownIdentity;

    @PostConstruct
    void init() {
        if (baseUrl != null && !baseUrl.isBlank()) {
            restClient = RestClient.builder().baseUrl(baseUrl).build();
            log.info("ParticipantPingClient enabled against {}", baseUrl);
        } else {
            log.info("ParticipantPingClient disabled (no participant.auth-api.base-url configured)");
        }
    }

    /**
     * Resolves the verified identity ({@code id} + {@code organization}) of the participant reachable at the given
     * connection endpoint URL, via the authentication-provider {@code ping} endpoint.
     *
     * @param connectionUrl the counterparty connector endpoint (e.g. the transfer/negotiation provider endpoint)
     * @return the resolved identity, or {@code null} when disabled/unknown/unavailable
     */
    public ParticipantIdentity resolveByEndpoint(String connectionUrl) {
        if (restClient == null || connectionUrl == null || connectionUrl.isBlank()) {
            return null;
        }
        String host;
        try {
            host = URI.create(connectionUrl).getHost();
        } catch (Exception e) {
            log.warn("ParticipantPingClient: could not parse connection url '{}': {}", connectionUrl, e.toString());
            return null;
        }
        if (host == null || host.isBlank()) {
            log.warn("ParticipantPingClient: no host in connection url '{}', skipping resolution", connectionUrl);
            return null;
        }
        // hostnames are case-insensitive, the cache key and the ping lookup must not be
        String fqdn = host.toLowerCase(Locale.ROOT);

        CachedIdentity cached = cache.get(fqdn);
        if (cached != null && !cached.isExpired()) {
            log.debug("ParticipantPingClient: cache hit for fqdn {} ({})", fqdn, cached.identity());
            return cached.identity();
        }
        ParticipantIdentity identity = fetch(fqdn);
        cache.put(fqdn, cacheEntry(identity));
        return identity;
    }

    /**
     * Resolves this connector's own verified identity ({@code id} + {@code organization}) from the
     * authentication-provider, used to label the consumer side of a transfer. The own identity is constant, so it is
     * resolved once and cached under the same TTL policy as {@link #resolveByEndpoint(String)}.
     *
     * @return the own identity, or {@code null} when disabled/unavailable
     */
    public ParticipantIdentity resolveOwn() {
        if (restClient == null) {
            return null;
        }
        CachedIdentity cached = ownIdentity;
        if (cached != null && !cached.isExpired()) {
            log.debug("ParticipantPingClient: cache hit for own identity ({})", cached.identity());
            return cached.identity();
        }
        ParticipantIdentity identity = fetchSelf();
        ownIdentity = cacheEntry(identity);
        return identity;
    }

    /**
     * Wraps a resolution outcome with its expiry: a resolved identity is kept for {@code cache.ttl-seconds}, an
     * unresolved one only for {@code cache.negative-ttl-seconds} so a transient authentication-provider failure is
     * retried instead of sticking for the whole life of the process.
     */
    private CachedIdentity cacheEntry(ParticipantIdentity identity) {
        long ttlSeconds = identity == null ? negativeCacheTtlSeconds : cacheTtlSeconds;
        return new CachedIdentity(identity, System.currentTimeMillis() + ttlSeconds * 1000L);
    }

    private ParticipantIdentity fetch(String fqdn) {
        try {
            JsonNode response = restClient.get()
                    .uri(builder -> builder.path(pingPath).queryParam("fqdn", fqdn).build())
                    .retrieve()
                    .body(JsonNode.class);
            return toIdentity(response);
        } catch (Exception e) {
            log.warn("ParticipantPingClient: could not resolve identity for fqdn {}: {}", fqdn, e.toString());
            return null;
        }
    }

    private ParticipantIdentity fetchSelf() {
        try {
            JsonNode response = restClient.get().uri(selfPath).retrieve().body(JsonNode.class);
            return toIdentity(response);
        } catch (Exception e) {
            log.warn("ParticipantPingClient: could not resolve own identity from {}: {}", selfPath, e.toString());
            return null;
        }
    }

    private static ParticipantIdentity toIdentity(JsonNode response) {
        if (response == null) {
            return null;
        }
        String id = response.path("id").asText(null);
        String organization = response.path("organization").asText(null);
        if ((id == null || id.isBlank()) && (organization == null || organization.isBlank())) {
            return null;
        }
        return new ParticipantIdentity(
                id == null || id.isBlank() ? null : id,
                organization == null || organization.isBlank() ? null : organization);
    }

    /** Verified participant identity returned by the authentication-provider ping. */
    public record ParticipantIdentity(String id, String organization) {}

    /** A cached resolution outcome; {@code identity} is {@code null} for a cached miss. */
    private record CachedIdentity(ParticipantIdentity identity, long expiresAtMillis) {

        boolean isExpired() {
            return System.currentTimeMillis() >= expiresAtMillis;
        }
    }
}
