package com.github.leoyakubov.twofactorauth.config;

import com.github.leoyakubov.twofactorauth.config.properties.JwtConfigProperties;
import com.github.leoyakubov.twofactorauth.config.properties.FrontendProperties;
import com.github.leoyakubov.twofactorauth.model.AuthUserDetails;
import com.github.leoyakubov.twofactorauth.model.Profile;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import com.github.leoyakubov.twofactorauth.service.JwtTokenService;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Set;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class JwtTokenAuthenticationFilterTest {

    private final JwtConfigProperties jwtConfig = createJwtConfig();
    private final JwtCookieManager cookieManager = new JwtCookieManager(
            jwtConfig,
            new FrontendProperties("http://localhost:3000"));
    private final JwtTokenService jwtTokenService = Mockito.mock(JwtTokenService.class);
    private final UserDetailsService userDetailsService = Mockito.mock(UserDetailsService.class);
    private final JwtTokenAuthenticationFilter filter =
            new JwtTokenAuthenticationFilter(cookieManager, jwtTokenService, userDetailsService);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPopulateSecurityContextWhenTokenIsReadFromCookie() throws Exception {
        User user = buildUser("demo");
        String token = "jwt-token";

        when(jwtTokenService.validateToken(token)).thenReturn(true);
        when(jwtTokenService.getClaimsFromJWT(token)).thenReturn(
                Jwts.claims().subject("demo").build());
        when(userDetailsService.loadUserByUsername("demo")).thenReturn(new AuthUserDetails(user));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        request.setCookies(new Cookie("AUTH_TOKEN", token));

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        AuthUserDetails principal = (AuthUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        assertEquals("demo", principal.getUsername());
    }

    @Test
    void shouldPopulateSecurityContextWhenBearerTokenIsTrimmed() throws Exception {
        User user = buildUser("demo");
        String token = "jwt-token";

        when(jwtTokenService.validateToken(token)).thenReturn(true);
        when(jwtTokenService.getClaimsFromJWT(token)).thenReturn(
                Jwts.claims().subject("demo").build());
        when(userDetailsService.loadUserByUsername("demo")).thenReturn(new AuthUserDetails(user));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        request.addHeader("Authorization", "Bearer   " + token + "   ");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        AuthUserDetails principal = (AuthUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        assertEquals("demo", principal.getUsername());
    }

    @Test
    void shouldClearSecurityContextWhenTokenIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me");
        request.setCookies(new Cookie("AUTH_TOKEN", "invalid"));

        when(jwtTokenService.validateToken("invalid")).thenReturn(false);

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
