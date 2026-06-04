package com.github.leoyakubov.twofactorauth.config;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtCookieManagerTest {

    private final JwtCookieManager cookieManager = new JwtCookieManager(createJwtConfig());

    @Test
    void createCookieShouldSetSecurityAttributes() {
        ResponseCookie cookie = cookieManager.createCookie("jwt-token");

        assertEquals("AUTH_TOKEN", cookie.getName());
        assertEquals("jwt-token", cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertTrue(cookie.isHttpOnly());
        assertFalse(cookie.isSecure());
    }

    @Test
    void clearCookieShouldExpireTheToken() {
        ResponseCookie cookie = cookieManager.clearCookie();

        assertEquals("AUTH_TOKEN", cookie.getName());
        assertEquals("", cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertTrue(cookie.isHttpOnly());
        assertEquals(Duration.ZERO, cookie.getMaxAge());
    }

    @Test
    void resolveTokenShouldPreferAuthorizationHeaderThenCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer header-token");
        request.setCookies(new Cookie("AUTH_TOKEN", "cookie-token"));

        assertEquals("header-token", cookieManager.resolveToken(request));
    }

    @Test
    void resolveTokenShouldFallBackToCookieWhenHeaderMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("AUTH_TOKEN", "cookie-token"));

        assertEquals("cookie-token", cookieManager.resolveToken(request));
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
