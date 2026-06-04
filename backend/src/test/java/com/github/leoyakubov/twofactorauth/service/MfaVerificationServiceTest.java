package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.exception.BadRequestException;
import com.github.leoyakubov.twofactorauth.exception.ResourceNotFoundException;
import com.github.leoyakubov.twofactorauth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MfaVerificationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private TotpService totpService;

    @Mock
    private RecoveryCodeService recoveryCodeService;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private AuthAttemptService authAttemptService;

    private MfaVerificationService mfaVerificationService;

    @BeforeEach
    void setUp() {
        mfaVerificationService = new MfaVerificationService(userService, totpService, recoveryCodeService,
                jwtTokenService, authAttemptService);
    }

    @Test
    void shouldReturnJwtWhenVerificationCodeIsValid() {
        User user = UserServiceTest.buildUser("demo", "demo@example.com", "encoded-secret", true);
        user.setSecret("mfa-secret");

        when(userService.getByUsername("demo")).thenReturn(user);
        when(totpService.verifyCode("123456", "mfa-secret")).thenReturn(true);
        when(jwtTokenService.generateToken(any())).thenReturn("jwt-token");

        String token = mfaVerificationService.verify("demo", "123456", "127.0.0.1");

        assertEquals("jwt-token", token);
    }

    @Test
    void shouldRejectInvalidCodeWhenVerificationCodeIsWrong() {
        User user = UserServiceTest.buildUser("demo", "demo@example.com", "encoded-secret", true);
        user.setSecret("mfa-secret");

        when(userService.getByUsername("demo")).thenReturn(user);
        when(totpService.verifyCode("000000", "mfa-secret")).thenReturn(false);
        when(recoveryCodeService.consumeRecoveryCode(user, "000000")).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> mfaVerificationService.verify("demo", "000000", "127.0.0.1"));
    }

    @Test
    void shouldAcceptRecoveryCodeAndConsumeItWhenVerificationCodeMatchesRecoveryCode() {
        User user = UserServiceTest.buildUser("demo", "demo@example.com", "encoded-secret", true);
        user.setSecret("mfa-secret");

        when(userService.getByUsername("demo")).thenReturn(user);
        when(totpService.verifyCode("ABCD-EFGH", "mfa-secret")).thenReturn(false);
        when(recoveryCodeService.consumeRecoveryCode(user, "ABCD-EFGH")).thenReturn(true);
        when(jwtTokenService.generateToken(any())).thenReturn("jwt-token");

        String token = mfaVerificationService.verify("demo", "ABCD-EFGH", "127.0.0.1");

        assertEquals("jwt-token", token);
        verify(userService).save(user);
    }

    @Test
    void shouldRejectUnknownUserWhenVerifyingCode() {
        when(userService.getByUsername("missing")).thenThrow(new ResourceNotFoundException("username missing"));

        assertThrows(ResourceNotFoundException.class,
                () -> mfaVerificationService.verify("missing", "123456", "127.0.0.1"));
    }
}
