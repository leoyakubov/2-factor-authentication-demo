package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.config.properties.JwtConfigProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenServiceTest {

    private final JwtConfigProperties jwtConfig = createJwtConfig();
    private final JwtTokenService jwtTokenService = new JwtTokenService(jwtConfig);

    @Test
    void shouldIncludeSubjectAndAuthoritiesWhenGeneratingToken() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "demo",
                        "secret",
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        String token = jwtTokenService.generateToken(authentication);

        assertTrue(jwtTokenService.validateToken(token));

        Claims claims = jwtTokenService.getClaimsFromJWT(token);
        assertEquals("demo", claims.getSubject());
        assertEquals(List.of("ROLE_USER"), claims.get("authorities", List.class));
    }

    @Test
    void shouldRejectMalformedTokensWhenValidatingToken() {
        assertFalse(jwtTokenService.validateToken("not-a-token"));
    }

    private static JwtConfigProperties createJwtConfig() {
        return new JwtConfigProperties(
                "Authorization",
                "Bearer",
                Duration.ofHours(1),
                "01234567890123456789012345678901",
                "AUTH_TOKEN",
                "/",
                false,
                "Strict"
        );
    }
}
