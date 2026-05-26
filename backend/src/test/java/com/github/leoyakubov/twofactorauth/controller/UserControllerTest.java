package com.github.leoyakubov.twofactorauth.controller;

import com.github.leoyakubov.twofactorauth.model.AuthUserDetails;
import com.github.leoyakubov.twofactorauth.model.Profile;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import com.github.leoyakubov.twofactorauth.payload.UserSummary;
import com.github.leoyakubov.twofactorauth.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.github.leoyakubov.twofactorauth.exception.ApiExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserController userController;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void findUserShouldReturnSummaryWithoutSecret() throws Exception {
        User user = buildUser("demo", "Demo User");
        when(userService.findByUsername("demo")).thenReturn(java.util.Optional.of(user));

        mockMvc.perform(get("/users/demo").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("demo")))
                .andExpect(jsonPath("$.name", is("Demo User")))
                .andExpect(jsonPath("$.profilePicture", is("https://example.com/demo.png")));
    }

    @Test
    void findAllShouldReturnListOfSummaries() throws Exception {
        when(userService.findAll()).thenReturn(List.of(buildUser("demo", "Demo User")));

        mockMvc.perform(get("/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("demo")));
    }

    @Test
    void findUserShouldReturnNotFoundMessageForMissingUser() throws Exception {
        when(userService.findByUsername("missing")).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/users/missing").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("We couldn't find an account with that username or email.")));
    }

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
