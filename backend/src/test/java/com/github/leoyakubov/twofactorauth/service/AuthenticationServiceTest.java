package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.model.AuthUserDetails;
import com.github.leoyakubov.twofactorauth.model.User;
import com.github.leoyakubov.twofactorauth.payload.LoginResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private AuthAttemptService authAttemptService;

    @Captor
    private ArgumentCaptor<Authentication> authenticationCaptor;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(authenticationManager, jwtTokenService, authAttemptService);
    }

    @Test
    void shouldReturnJwtWhenLoginSucceedsForUserWithoutMfa() {
        User user = UserServiceTest.buildUser("demo", "demo@example.com", "encoded-secret", false);

        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(new AuthUserDetails(user), null, new AuthUserDetails(user).getAuthorities()));
        when(jwtTokenService.generateToken(authenticationCaptor.capture())).thenReturn("jwt-token");

        LoginResult result = authenticationService.login("demo", "secret", "127.0.0.1");

        assertEquals("jwt-token", result.accessToken());
        assertEquals("demo", authenticationCaptor.getValue().getName());
        assertFalse(result.mfaRequired());
    }

    @Test
    void shouldRequestMfaWhenMfaUserLogsIn() {
        User user = UserServiceTest.buildUser("demo", "demo@example.com", "encoded-secret", true);
        user.setSecret("mfa-secret");

        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(new AuthUserDetails(user), null, new AuthUserDetails(user).getAuthorities()));

        LoginResult result = authenticationService.login("demo", "secret", "127.0.0.1");

        assertEquals(true, result.mfaRequired());
        assertEquals(null, result.accessToken());
        verify(jwtTokenService, never()).generateToken(any());
    }

    @Test
    void shouldRecordFailureWhenLoginFails() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));

        org.junit.jupiter.api.Assertions.assertThrows(BadCredentialsException.class,
                () -> authenticationService.login("demo", "wrong", "127.0.0.1"));

        verify(authAttemptService).recordFailure(AuthAttemptService.AuthAttemptAction.SIGN_IN, "demo", "127.0.0.1");
    }
}
