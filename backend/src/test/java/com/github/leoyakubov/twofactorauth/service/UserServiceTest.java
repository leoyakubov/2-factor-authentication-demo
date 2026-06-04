package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.exception.ResourceNotFoundException;
import com.github.leoyakubov.twofactorauth.model.Profile;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import com.github.leoyakubov.twofactorauth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldFindUserByUsernameWhenUserExists() {
        User user = buildUser("demo", "demo@example.com", "encoded-secret", false);
        UserService userService = new UserService(userRepository);

        when(userRepository.findByUsername("demo")).thenReturn(Optional.of(user));

        assertEquals(Optional.of(user), userService.findByUsername("demo"));
    }

    @Test
    void shouldFindUserByUsernameOrEmailWhenEmailMatches() {
        User user = buildUser("demo", "demo@example.com", "encoded-secret", false);
        UserService userService = new UserService(userRepository);

        when(userRepository.findByUsername("demo@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("demo@example.com")).thenReturn(Optional.of(user));

        assertEquals(Optional.of(user), userService.findByUsernameOrEmail("demo@example.com"));
    }

    @Test
    void shouldThrowWhenGettingMissingUserByUsername() {
        UserService userService = new UserService(userRepository);

        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getByUsername("missing"));
    }

    @Test
    void shouldPreserveMfaStateWhenCopyingUser() {
        User user = buildUser("demo", "demo@example.com", "secret", true);
        user.setSecret("mfa-secret");

        User copied = new User(user);

        assertTrue(copied.isMfa());
        assertEquals("mfa-secret", copied.getSecret());
    }

    static User buildUser(String username, String email, String password, boolean mfa) {
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
