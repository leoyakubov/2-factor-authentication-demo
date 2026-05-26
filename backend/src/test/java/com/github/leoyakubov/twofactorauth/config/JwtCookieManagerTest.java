package com.github.leoyakubov.twofactorauth.config;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;

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
        assertEquals(0, cookie.getMaxAge().getSeconds());
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
        JwtConfigProperties config = new JwtConfigProperties();
        config.setHeader("Authorization");
        config.setPrefix("Bearer");
        config.setExpiration(3600);
        config.setSecret("01234567890123456789012345678901");
        config.setCookieName("AUTH_TOKEN");
        config.setCookiePath("/");
        config.setCookieSecure(false);
        config.setCookieSameSite("Strict");
        return config;
    }
}
