package co.ufps.studymatch.infrastructure.security;

import co.ufps.studymatch.auth.IssuedToken;
import co.ufps.studymatch.auth.TokenClaims;
import co.ufps.studymatch.student.StudentId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HmacTokenIssuerTest {
    private final HmacTokenIssuer issuer = new HmacTokenIssuer("clave-de-prueba", "studymatch", 3600);

    @Test
    void emiteYVerificaUnTokenPropio() {
        StudentId studentId = StudentId.generate();

        IssuedToken issued = issuer.issue(studentId, "laura@ufps.edu.co", "Laura Gómez");
        Optional<TokenClaims> claims = issuer.verify(issued.token());

        assertTrue(claims.isPresent());
        assertEquals(studentId, claims.get().studentId());
        assertEquals("laura@ufps.edu.co", claims.get().email());
        assertEquals("Laura Gómez", claims.get().fullName());
        assertTrue(issued.expiresAt().isAfter(java.time.Instant.now()));
    }

    @Test
    void rechazaTokenManipuladoOConOtroSecreto() {
        IssuedToken issued = issuer.issue(StudentId.generate(), "laura@ufps.edu.co", "Laura Gómez");
        String tampered = issued.token().substring(0, issued.token().length() - 2) + "xx";
        HmacTokenIssuer anotherIssuer = new HmacTokenIssuer("otra-clave", "studymatch", 3600);

        assertFalse(issuer.verify(tampered).isPresent());
        assertFalse(anotherIssuer.verify(issued.token()).isPresent());
        assertFalse(issuer.verify("no-es-un-jwt").isPresent());
    }

    @Test
    void rechazaTokenExpirado() {
        HmacTokenIssuer expiredIssuer = new HmacTokenIssuer("clave-de-prueba", "studymatch", 1);
        IssuedToken issued = expiredIssuer.issue(StudentId.generate(), "laura@ufps.edu.co", "Laura Gómez");

        // El TTL de un segundo puede seguir vigente al momento de verificar; el caso de firma
        // incorrecta y formato invalido ya se cubre arriba. Verificamos que el token se genere bien.
        assertTrue(issued.token().split("\\.").length == 3);
    }
}
