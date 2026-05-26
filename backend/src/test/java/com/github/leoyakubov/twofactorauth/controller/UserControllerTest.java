package com.github.leoyakubov.twofactorauth.controller;

import com.github.leoyakubov.twofactorauth.model.AuthUserDetails;
import com.github.leoyakubov.twofactorauth.model.Profile;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import com.github.leoyakubov.twofactorauth.payload.UserSummary;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserControllerTest {

    private final UserController userController = new UserController();

    @Test
    void getCurrentUserShouldMapPrincipalToSummary() {
        User user = buildUser("demo", "Demo User");
        user.setId("user-1");
        AuthUserDetails details = new AuthUserDetails(user);

        UserSummary summary = userController.getCurrentUser(details);

        assertEquals("user-1", summary.getId());
        assertEquals("demo", summary.getUsername());
        assertEquals("Demo User", summary.getName());
        assertEquals("https://example.com/demo.png", summary.getProfilePicture());
    }

    private static User buildUser(String username, String displayName) {
        return User.builder()
                .username(username)
                .email(username + "@example.com")
                .password("encoded")
                .active(true)
                .userProfile(Profile.builder()
                        .displayName(displayName)
                        .profilePictureUrl("https://example.com/" + username + ".png")
                        .build())
                .roles(Set.of(Role.USER))
                .mfa(false)
                .build();
    }
}
