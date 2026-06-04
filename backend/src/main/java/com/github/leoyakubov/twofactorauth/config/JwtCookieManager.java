package com.github.leoyakubov.twofactorauth.config;

import com.github.leoyakubov.twofactorauth.config.properties.JwtConfigProperties;
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
        return ResponseCookie.from(jwtConfig.cookieName(), token)
                .httpOnly(true)
                .secure(jwtConfig.cookieSecure())
                .path(jwtConfig.cookiePath())
                .sameSite(jwtConfig.cookieSameSite())
                .maxAge(jwtConfig.expiration())
                .build();
    }

    public ResponseCookie clearCookie() {
        return ResponseCookie.from(jwtConfig.cookieName(), "")
                .httpOnly(true)
                .secure(jwtConfig.cookieSecure())
                .path(jwtConfig.cookiePath())
                .sameSite(jwtConfig.cookieSameSite())
                .maxAge(Duration.ZERO)
                .build();
    }

    public String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(jwtConfig.header());
        if (header != null && header.startsWith(jwtConfig.prefix())) {
            return header.substring(jwtConfig.prefix().length()).trim();
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> jwtConfig.cookieName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
