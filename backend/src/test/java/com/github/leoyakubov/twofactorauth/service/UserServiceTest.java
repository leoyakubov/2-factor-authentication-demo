package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.exception.BadRequestException;
import com.github.leoyakubov.twofactorauth.exception.EmailAlreadyExistsException;
import com.github.leoyakubov.twofactorauth.exception.ResourceNotFoundException;
import com.github.leoyakubov.twofactorauth.exception.UsernameAlreadyExistsException;
import com.github.leoyakubov.twofactorauth.model.AuthUserDetails;
import com.github.leoyakubov.twofactorauth.model.Profile;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import com.github.leoyakubov.twofactorauth.repository.UserRepository;
import com.github.leoyakubov.twofactorauth.payload.LoginResult;
import com.github.leoyakubov.twofactorauth.payload.RegistrationResult;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private TotpService totpService;

    @Mock
    private RecoveryCodeService recoveryCodeService;

    @Mock
    private AuthAttemptService authAttemptService;

    @Captor
    private ArgumentCaptor<Authentication> authenticationCaptor;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(passwordEncoder, userRepository, authenticationManager, jwtTokenService,
                totpService, recoveryCodeService, authAttemptService);
    }

    @Test
    void shouldEncodePasswordAndPersistSecretForMfaWhenRegisteringUser() {
        User user = buildUser("demo", "demo@example.com", "secret", true);
        java.util.List<String> recoveryCodes = java.util.List.of("ABCD-EFGH");

        when(userRepository.existsByUsername("demo")).thenReturn(false);
        when(userRepository.existsByEmail("demo@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(totpService.generateSecret()).thenReturn("mfa-secret");
        when(recoveryCodeService.generateRecoveryCodes()).thenReturn(recoveryCodes);
        when(recoveryCodeService.hashCodes(recoveryCodes)).thenReturn(Set.of("hashed-recovery"));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.registerUser(user, Role.USER);

        assertEquals("encoded-secret", saved.getPassword());
        assertEquals("mfa-secret", saved.getSecret());
        assertTrue(saved.isActive());
        assertEquals(Set.of(Role.USER), saved.getRoles());
        assertEquals(Set.of("hashed-recovery"), saved.getRecoveryCodes());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRejectExistingUsernameWhenRegisteringUser() {
        User user = buildUser("demo", "demo@example.com", "secret", false);

        when(userRepository.existsByUsername("demo")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> userService.registerUser(user, Role.USER));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectExistingEmailWhenRegisteringUser() {
        User user = buildUser("demo", "demo@example.com", "secret", false);

        when(userRepository.existsByUsername("demo")).thenReturn(false);
        when(userRepository.existsByEmail("demo@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(user, Role.USER));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldReturnJwtForUsernameWhenLoginSucceeds() {
        User user = buildUser("demo", "demo@example.com", "encoded-secret", false);

        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(new AuthUserDetails(user), null, new AuthUserDetails(user).getAuthorities()));
        when(jwtTokenService.generateToken(authenticationCaptor.capture())).thenReturn("jwt-token");

        LoginResult result = userService.loginUser("demo", "secret");

        assertEquals("jwt-token", result.accessToken());
        assertEquals("demo", authenticationCaptor.getValue().getName());
        assertFalse(result.mfaRequired());
    }

    @Test
    void shouldReturnJwtForEmailWhenLoginSucceeds() {
        User user = buildUser("demo", "demo@example.com", "encoded-secret", false);

        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(new AuthUserDetails(user), null, new AuthUserDetails(user).getAuthorities()));
        when(jwtTokenService.generateToken(any())).thenReturn("jwt-token");

        LoginResult result = userService.loginUser("demo@example.com", "secret");

        assertEquals("jwt-token", result.accessToken());
        assertFalse(result.mfaRequired());
        verify(jwtTokenService).generateToken(any());
    }

    @Test
    void shouldRequestMfaWhenMfaUserLogsIn() {
        User user = buildUser("demo", "demo@example.com", "encoded-secret", true);
        user.setSecret("mfa-secret");

        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(new AuthUserDetails(user), null, new AuthUserDetails(user).getAuthorities()));

        LoginResult result = userService.loginUser("demo", "secret");

        assertEquals(true, result.mfaRequired());
        assertEquals(null, result.accessToken());
        verify(jwtTokenService, never()).generateToken(any());
    }

    @Test
    void shouldPropagateBadCredentialsWhenLoginFails() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));

        assertThrows(BadCredentialsException.class, () -> userService.loginUser("demo", "wrong"));
    }

    @Test
    void shouldReturnJwtWhenVerificationCodeIsValid() {
        User user = buildUser("demo", "demo@example.com", "encoded-secret", true);
        user.setSecret("mfa-secret");

        when(userRepository.findByUsername("demo")).thenReturn(Optional.of(user));
        when(totpService.verifyCode("123456", "mfa-secret")).thenReturn(true);
        when(jwtTokenService.generateToken(any())).thenReturn("jwt-token");

        String token = userService.verify("demo", "123456");

        assertEquals("jwt-token", token);
    }

    @Test
    void shouldRejectInvalidCodeWhenVerificationCodeIsWrong() {
        User user = buildUser("demo", "demo@example.com", "encoded-secret", true);
        user.setSecret("mfa-secret");

        when(userRepository.findByUsername("demo")).thenReturn(Optional.of(user));
        when(totpService.verifyCode("000000", "mfa-secret")).thenReturn(false);
        when(recoveryCodeService.consumeRecoveryCode(user, "000000")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> userService.verify("demo", "000000"));
    }

    @Test
    void shouldAcceptRecoveryCodeAndConsumeItWhenVerificationCodeMatchesRecoveryCode() {
        User user = buildUser("demo", "demo@example.com", "encoded-secret", true);
        user.setSecret("mfa-secret");

        when(userRepository.findByUsername("demo")).thenReturn(Optional.of(user));
        when(totpService.verifyCode("ABCD-EFGH", "mfa-secret")).thenReturn(false);
        when(recoveryCodeService.consumeRecoveryCode(user, "ABCD-EFGH")).thenReturn(true);
        when(jwtTokenService.generateToken(any())).thenReturn("jwt-token");

        String token = userService.verify("demo", "ABCD-EFGH");

        assertEquals("jwt-token", token);
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectUnknownUserWhenVerifyingCode() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.verify("missing", "123456"));
    }

    @Test
    void shouldPreserveMfaStateWhenCopyingUser() {
        User user = buildUser("demo", "demo@example.com", "secret", true);
        user.setSecret("mfa-secret");

        User copied = new User(user);

        assertTrue(copied.isMfa());
        assertEquals("mfa-secret", copied.getSecret());
    }

    private static User buildUser(String username, String email, String password, boolean mfa) {
        return User.builder()
                .username(username)
                .email(email)
                .password(password)
                .active(true)
                .userProfile(Profile.builder().displayName("Demo User").build())
                .roles(Set.of(Role.USER))
                .mfa(mfa)
                .build();
    }
}
