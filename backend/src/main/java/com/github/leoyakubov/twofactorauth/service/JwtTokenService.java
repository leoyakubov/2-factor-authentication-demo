package com.github.leoyakubov.twofactorauth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import com.github.leoyakubov.twofactorauth.config.properties.JwtConfigProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;


@Service
@Slf4j
public class JwtTokenService {

    private final JwtConfigProperties jwtConfig;

    public JwtTokenService(JwtConfigProperties jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String generateToken(Authentication authentication) {

        Long now = System.currentTimeMillis();
        SecretKey key = signingKey();
        return Jwts.builder()
                .subject(authentication.getName())
                .claim("authorities", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtConfig.expiration().toMillis()))
                .signWith(key)
                .compact();
    }

    public Claims getClaimsFromJWT(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(authToken);

            return true;
        } catch (SignatureException ex) {
            log.debug("invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.debug("invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.debug("expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.debug("unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.debug("JWT claims string is empty");
        }
        return false;
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtConfig.secret().getBytes(StandardCharsets.UTF_8));
    }
}
