package com.github.leoyakubov.twofactorauth.controller;

import com.github.leoyakubov.twofactorauth.model.Profile;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import com.github.leoyakubov.twofactorauth.payload.JwtAuthenticationResponse;
import com.github.leoyakubov.twofactorauth.payload.LoginRequest;
import com.github.leoyakubov.twofactorauth.payload.SignUpRequest;
import com.github.leoyakubov.twofactorauth.payload.SignupResponse;
import com.github.leoyakubov.twofactorauth.payload.VerifyCodeRequest;
import com.github.leoyakubov.twofactorauth.service.TotpManager;
import com.github.leoyakubov.twofactorauth.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.github.leoyakubov.twofactorauth.exception.ApiExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TotpManager totpManager;

    @Test
    void signInShouldReturnJwtResponse() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("demo");
        request.setPassword("secret");

        when(userService.loginUser("demo", "secret")).thenReturn("jwt-token");

        mockMvc.perform(post("/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("jwt-token")))
                .andExpect(jsonPath("$.mfa", is(false)));
    }

    @Test
    void signInShouldReturnFriendlyValidationError() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("demo");

        mockMvc.perform(post("/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Please check the form fields and try again.")));
    }

    @Test
    void verifyShouldReturnJwtResponse() throws Exception {
        VerifyCodeRequest request = new VerifyCodeRequest();
        request.setUsername("demo");
        request.setCode("123456");

        when(userService.verify("demo", "123456")).thenReturn("jwt-token");

        mockMvc.perform(post("/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("jwt-token")))
                .andExpect(jsonPath("$.mfa", is(false)));
    }

    @Test
    void signupShouldReturnQrCodeWhenMfaEnabled() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setName("Demo User");
        request.setUsername("demo");
        request.setEmail("demo@example.com");
        request.setPassword("secret123");
        request.setMfa(true);

        User saved = User.builder()
                .id("123")
                .username("demo")
                .email("demo@example.com")
                .password("encoded")
                .active(true)
                .userProfile(Profile.builder().displayName("Demo User").build())
                .roles(Set.of(Role.USER))
                .mfa(true)
                .secret("mfa-secret")
                .build();

        when(userService.registerUser(any(User.class), eq(Role.USER))).thenReturn(saved);
        when(totpManager.getUriForImage("mfa-secret")).thenReturn("data:image/png;base64,qr");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mfa", is(true)))
                .andExpect(jsonPath("$.secretImageUri", is("data:image/png;base64,qr")));
    }
}
