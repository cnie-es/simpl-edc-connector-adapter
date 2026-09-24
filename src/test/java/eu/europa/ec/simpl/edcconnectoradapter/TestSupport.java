package eu.europa.ec.simpl.edcconnectoradapter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public final class TestSupport {

    private TestSupport() {}

    public static JSONObject getResourceAsJsonObj(String name, Charset charset) throws IOException {
        String value = getResourceAsString(name, charset);
        return new JSONObject(value);
    }

    /**
     *
     * @param name
     * @param charset (optional: null) default StandardCharsets.UTF_8
     * @return
     * @throws IOException
     */
    public static String getResourceAsString(String name, Charset charset) throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream contentStream = classloader.getResourceAsStream(name)) {
            if (contentStream == null) {
                throw new IllegalArgumentException("Resource not found: " + name);
            }
            return new String(contentStream.readAllBytes(), charset != null ? charset : StandardCharsets.UTF_8);
        }
    }

    public static InputStream getResourceAsStream(String name) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        return classloader.getResourceAsStream(name);
    }

    public static String createValidJwt() {
        return JWT.create()
                .withClaim("exp", 1730819669L)
                .withClaim("iat", 1730819369L)
                .withClaim("auth_time", 1730819368L)
                .withClaim("jti", "cdd742f4-e245-4e78-9a20-b5f5d6544bb7")
                .withClaim("iss", "https://t1.gaiax-edc-dev-sd.dev.simpl-europe.eu/auth/realms/participant")
                .withArrayClaim("aud", new String[] {"realm-management", "account"})
                .withClaim("sub", "4ac46130-3532-4843-9556-9ceb9ec20136")
                .withClaim("typ", "Bearer")
                .withClaim("azp", "frontend-cli")
                .withClaim("sid", "bb07f4d2-aa63-4929-93ad-4c263d85c2cf")
                .withClaim("acr", "1")
                .withArrayClaim("allowed-origins", new String[] {"*"})
                .withClaim(
                        "resource_access",
                        Map.of(
                                "realm-management",
                                        Map.of(
                                                "roles",
                                                List.of("view-realm", "view-users", "query-groups", "query-users")),
                                "frontend-cli", Map.of("roles", List.of("T1UAR_M", "ONBOARDER_M")),
                                "account",
                                        Map.of(
                                                "roles",
                                                List.of("manage-account", "manage-account-links", "view-profile"))))
                .withClaim("scope", "dsAttributes email profile")
                .withClaim("email_verified", true)
                .withClaim("participant_id", "0192e220-9c85-7068-b21a-122bdd34ebd7")
                .withClaim("name", "Alexander Williams")
                .withClaim("preferred_username", "a.w")
                .withClaim("given_name", "Alexander")
                .withClaim("family_name", "Williams")
                .withArrayClaim("client-roles", new String[] {"T1UAR_M", "ONBOARDER_M"})
                .withClaim("identity_attributes", List.of())
                .withClaim("email", "a.w@email.com")
                .withClaim(
                        "credential_id",
                        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEOyq5p2RG2IcSgXqVNyWPrTJ7rtwzf5he5BvI/BZX2KaGvv3NoFnBMGCbNDvaRz+Cwu3xCHwrq2WtuZaX/zVgow==")
                .sign(Algorithm.HMAC256("test-secret-key"));
    }

    public static String createNoCredentialsJwt() {
        return JWT.create()
                .withClaim("exp", 1730819669L)
                .withClaim("iat", 1730819369L)
                .withClaim("auth_time", 1730819368L)
                .withClaim("jti", "cdd742f4-e245-4e78-9a20-b5f5d6544bb7")
                .withClaim("iss", "https://t1.gaiax-edc-dev-sd.dev.simpl-europe.eu/auth/realms/participant")
                .withArrayClaim("aud", new String[] {"realm-management", "account"})
                .withClaim("sub", "4ac46130-3532-4843-9556-9ceb9ec20136")
                .withClaim("typ", "Bearer")
                .withClaim("azp", "frontend-cli")
                .withClaim("sid", "bb07f4d2-aa63-4929-93ad-4c263d85c2cf")
                .withClaim("acr", "1")
                .withArrayClaim("allowed-origins", new String[] {"*"})
                .withClaim(
                        "resource_access",
                        Map.of(
                                "realm-management",
                                        Map.of(
                                                "roles",
                                                List.of("view-realm", "view-users", "query-groups", "query-users")),
                                "frontend-cli", Map.of("roles", List.of("T1UAR_M", "ONBOARDER_M")),
                                "account",
                                        Map.of(
                                                "roles",
                                                List.of("manage-account", "manage-account-links", "view-profile"))))
                .withClaim("scope", "dsAttributes email profile")
                .withClaim("email_verified", true)
                .withClaim("participant_id", "0192e220-9c85-7068-b21a-122bdd34ebd7")
                .withClaim("name", "Alexander Williams")
                .withClaim("preferred_username", "a.w")
                .withClaim("given_name", "Alexander")
                .withClaim("family_name", "Williams")
                .withArrayClaim("client-roles", new String[] {"T1UAR_M", "ONBOARDER_M"})
                .withClaim("identity_attributes", List.of())
                .withClaim("email", "a.w@email.com")
                .withClaim(
                        "credential_id",
                        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEOyq5p2RG2IcSgXqVNyWPrTJ7rtwzf5he5BvI/BZX2KaGvv3NoFnBMGCbNDvaRz+Cwu3xCHwrq2WtuZaX/zVgow==")
                .sign(Algorithm.HMAC256("test-secret-key"));
    }
}
