package com.github.leoyakubov.twofactorauth.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;

@Component
public class JwtCookieManager {

    private final JwtConfigProperties jwtConfig;

    public JwtCookieManager(JwtConfigProperties jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public ResponseCookie createCookie(String token) {
        return ResponseCookie.from(jwtConfig.getCookieName(), token)
                .httpOnly(true)
                .secure(jwtConfig.isCookieSecure())
                .path(jwtConfig.getCookiePath())
                .sameSite(jwtConfig.getCookieSameSite())
                .maxAge(Duration.ofSeconds(jwtConfig.getExpiration()))
                .build();
    }

    public ResponseCookie clearCookie() {
        return ResponseCookie.from(jwtConfig.getCookieName(), "")
                .httpOnly(true)
                .secure(jwtConfig.isCookieSecure())
                .path(jwtConfig.getCookiePath())
                .sameSite(jwtConfig.getCookieSameSite())
                .maxAge(Duration.ZERO)
                .build();
    }

    public String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(jwtConfig.getHeader());
        if (header != null && header.startsWith(jwtConfig.getPrefix())) {
            return header.substring(jwtConfig.getPrefix().length()).trim();
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> jwtConfig.getCookieName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
