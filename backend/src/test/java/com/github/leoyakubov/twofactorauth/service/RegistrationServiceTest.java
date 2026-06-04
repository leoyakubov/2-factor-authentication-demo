package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.exception.EmailAlreadyExistsException;
import com.github.leoyakubov.twofactorauth.exception.UsernameAlreadyExistsException;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @Mock
    private TotpService totpService;

    @Mock
    private RecoveryCodeService recoveryCodeService;

    @Mock
    private AuthAttemptService authAttemptService;

    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(passwordEncoder, userService, totpService,
                recoveryCodeService, authAttemptService);
    }

    @Test
    void shouldEncodePasswordAndPersistSecretForMfaWhenRegisteringUser() {
        User user = UserServiceTest.buildUser("demo", "demo@example.com", "secret", true);
        List<String> recoveryCodes = List.of("ABCD-EFGH");

        when(userService.existsByUsername("demo")).thenReturn(false);
        when(userService.existsByEmail("demo@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(totpService.generateSecret()).thenReturn("mfa-secret");
        when(recoveryCodeService.generateRecoveryCodes()).thenReturn(recoveryCodes);
        when(recoveryCodeService.hashCodes(recoveryCodes)).thenReturn(Set.of("hashed-recovery"));
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = registrationService.register(user, Role.USER, "127.0.0.1").user();

        assertEquals("encoded-secret", saved.getPassword());
        assertEquals("mfa-secret", saved.getSecret());
        assertTrue(saved.isActive());
        assertEquals(Set.of(Role.USER), saved.getRoles());
        assertEquals(Set.of("hashed-recovery"), saved.getRecoveryCodes());
        verify(userService).save(any(User.class));
    }

    @Test
    void shouldRejectExistingUsernameWhenRegisteringUser() {
        User user = UserServiceTest.buildUser("demo", "demo@example.com", "secret", false);

        when(userService.existsByUsername("demo")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class,
                () -> registrationService.register(user, Role.USER, "127.0.0.1"));
        verify(userService, never()).save(any());
    }

    @Test
    void shouldRejectExistingEmailWhenRegisteringUser() {
        User user = UserServiceTest.buildUser("demo", "demo@example.com", "secret", false);

        when(userService.existsByUsername("demo")).thenReturn(false);
        when(userService.existsByEmail("demo@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> registrationService.register(user, Role.USER, "127.0.0.1"));
        verify(userService, never()).save(any());
    }
}
