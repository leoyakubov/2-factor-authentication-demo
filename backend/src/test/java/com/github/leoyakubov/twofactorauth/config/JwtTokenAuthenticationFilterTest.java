package com.github.leoyakubov.twofactorauth.config;

import com.github.leoyakubov.twofactorauth.model.AuthUserDetails;
import com.github.leoyakubov.twofactorauth.model.Profile;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import com.github.leoyakubov.twofactorauth.service.JwtTokenManager;
import com.github.leoyakubov.twofactorauth.service.UserService;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class JwtTokenAuthenticationFilterTest {

    private final JwtConfigProperties jwtConfig = createJwtConfig();
    private final JwtCookieManager cookieManager = new JwtCookieManager(jwtConfig);
    private final JwtTokenManager jwtTokenManager = Mockito.mock(JwtTokenManager.class);
    private final UserService userService = Mockito.mock(UserService.class);
    private final JwtTokenAuthenticationFilter filter =
            new JwtTokenAuthenticationFilter(cookieManager, jwtTokenManager, userService);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReadTokenFromCookieAndPopulateSecurityContext() throws Exception {
        User user = buildUser("demo");
        String token = "jwt-token";

        when(jwtTokenManager.validateToken(token)).thenReturn(true);
        when(jwtTokenManager.getClaimsFromJWT(token)).thenReturn(
                Jwts.claims().subject("demo").build());
        when(userService.findByUsername("demo")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        request.setCookies(new Cookie("AUTH_TOKEN", token));

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        AuthUserDetails principal = (AuthUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        assertEquals("demo", principal.getUsername());
    }

    @Test
    void shouldTrimBearerTokenAndPopulateSecurityContext() throws Exception {
        User user = buildUser("demo");
        String token = "jwt-token";

        when(jwtTokenManager.validateToken(token)).thenReturn(true);
        when(jwtTokenManager.getClaimsFromJWT(token)).thenReturn(
                Jwts.claims().subject("demo").build());
        when(userService.findByUsername("demo")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        request.addHeader("Authorization", "Bearer   " + token + "   ");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        AuthUserDetails principal = (AuthUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        assertEquals("demo", principal.getUsername());
    }

    @Test
    void shouldClearContextForInvalidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        request.setCookies(new Cookie("AUTH_TOKEN", "invalid"));

        when(jwtTokenManager.validateToken("invalid")).thenReturn(false);

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
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

    private static User buildUser(String username) {
        return User.builder()
                .username(username)
                .email(username + "@example.com")
                .password("encoded")
                .active(true)
                .userProfile(Profile.builder().displayName("Demo User").build())
                .roles(Set.of(Role.USER))
                .mfa(false)
                .build();
    }
}
