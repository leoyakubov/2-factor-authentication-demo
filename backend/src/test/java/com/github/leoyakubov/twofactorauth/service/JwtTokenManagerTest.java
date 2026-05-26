package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.config.JwtConfigProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenManagerTest {

    private final JwtConfigProperties jwtConfig = createJwtConfig();
    private final JwtTokenManager jwtTokenManager = new JwtTokenManager(jwtConfig);

    @Test
    void generateTokenShouldIncludeSubjectAndAuthorities() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "demo",
                        "secret",
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        String token = jwtTokenManager.generateToken(authentication);

        assertTrue(jwtTokenManager.validateToken(token));

        Claims claims = jwtTokenManager.getClaimsFromJWT(token);
        assertEquals("demo", claims.getSubject());
        assertEquals(List.of("ROLE_USER"), claims.get("authorities", List.class));
    }

    @Test
    void validateTokenShouldRejectMalformedTokens() {
        assertFalse(jwtTokenManager.validateToken("not-a-token"));
    }

    private static JwtConfigProperties createJwtConfig() {
        JwtConfigProperties config = new JwtConfigProperties();
        config.setHeader("Authorization");
        config.setPrefix("Bearer");
        config.setExpiration(3600);
        config.setSecret("01234567890123456789012345678901");
        return config;
    }
}
