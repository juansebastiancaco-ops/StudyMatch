package co.ufps.studymatch.infrastructure.security;

import co.ufps.studymatch.auth.IssuedToken;
import co.ufps.studymatch.auth.TokenClaims;
import co.ufps.studymatch.auth.TokenIssuer;
import co.ufps.studymatch.student.StudentId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * JWT HS256 sin dependencia de un proveedor externo. El token incluye sub, email, name, iat, exp e iss.
 */
@ApplicationScoped
public class HmacTokenIssuer implements TokenIssuer {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String secret;
    private final String issuer;
    private final long ttlSeconds;

    @Inject
    public HmacTokenIssuer(
            @ConfigProperty(name = "studymatch.security.jwt.secret") String secret,
            @ConfigProperty(name = "studymatch.security.jwt.issuer", defaultValue = "studymatch") String issuer,
            @ConfigProperty(name = "studymatch.security.jwt.ttl-seconds", defaultValue = "86400") long ttlSeconds) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("El secreto JWT es obligatorio");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("El issuer JWT es obligatorio");
        }
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("El TTL del JWT debe ser mayor que cero");
        }
        this.secret = secret;
        this.issuer = issuer.strip();
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public IssuedToken issue(StudentId studentId, String email, String fullName) {
        try {
            long issuedAt = Instant.now().getEpochSecond();
            long expiresAt = issuedAt + ttlSeconds;
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("iss", issuer);
            payload.put("sub", studentId.value());
            payload.put("email", email);
            payload.put("name", fullName);
            payload.put("iat", issuedAt);
            payload.put("exp", expiresAt);

            String encodedHeader = encode(MAPPER.writeValueAsBytes(header));
            String encodedPayload = encode(MAPPER.writeValueAsBytes(payload));
            String unsigned = encodedHeader + "." + encodedPayload;
            String signature = sign(unsigned);
            return new IssuedToken(unsigned + "." + signature, Instant.ofEpochSecond(expiresAt));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el token JWT", e);
        }
    }

    @Override
    public Optional<TokenClaims> verify(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Optional.empty();
            }
            String unsigned = parts[0] + "." + parts[1];
            String expected = sign(unsigned);
            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                return Optional.empty();
            }
            JsonNode payload = MAPPER.readTree(Base64.getUrlDecoder().decode(parts[1]));
            long expiresAt = payload.path("exp").asLong(0);
            if (expiresAt <= Instant.now().getEpochSecond()) {
                return Optional.empty();
            }
            if (!issuer.equals(payload.path("iss").asText())) {
                return Optional.empty();
            }
            String subject = payload.path("sub").asText(null);
            if (subject == null || subject.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(new TokenClaims(
                    StudentId.of(subject),
                    payload.path("email").asText(""),
                    payload.path("name").asText(""),
                    expiresAt
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String sign(String unsigned) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        return encode(mac.doFinal(unsigned.getBytes(StandardCharsets.UTF_8)));
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
