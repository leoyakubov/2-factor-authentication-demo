package com.github.leoyakubov.twofactorauth.controller;

import com.github.leoyakubov.twofactorauth.config.JwtCookieManager;
import com.github.leoyakubov.twofactorauth.exception.ApiExceptionHandler;
import com.github.leoyakubov.twofactorauth.model.Profile;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import com.github.leoyakubov.twofactorauth.payload.LoginRequest;
import com.github.leoyakubov.twofactorauth.payload.LoginResult;
import com.github.leoyakubov.twofactorauth.payload.RegistrationResult;
import com.github.leoyakubov.twofactorauth.payload.SignUpRequest;
import com.github.leoyakubov.twofactorauth.payload.VerifyCodeRequest;
import com.github.leoyakubov.twofactorauth.service.TotpManager;
import com.github.leoyakubov.twofactorauth.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private TotpManager totpManager;

    @Mock
    private JwtCookieManager cookieManager;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(userService, totpManager, cookieManager))
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void signInShouldReturnJwtResponse() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("demo");
        request.setPassword("secret");

        when(userService.loginUser(eq("demo"), eq("secret"), anyString())).thenReturn(LoginResult.authenticated("jwt-token"));
        when(cookieManager.createCookie("jwt-token"))
                .thenReturn(ResponseCookie.from("AUTH_TOKEN", "jwt-token").httpOnly(true).path("/").build());

        mockMvc.perform(post("/signin")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("AUTH_TOKEN=jwt-token")))
                .andExpect(jsonPath("$.mfa", is(false)))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    void signInShouldReturnMfaRequiredResponse() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("demo");
        request.setPassword("secret");

        when(userService.loginUser(eq("demo"), eq("secret"), anyString())).thenReturn(LoginResult.requiresMfa());

        mockMvc.perform(post("/signin")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(jsonPath("$.mfa", is(true)))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    void signInShouldReturnFriendlyValidationError() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("demo");

        mockMvc.perform(post("/signin")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Please check the form fields and try again.")));
    }

    @Test
    void verifyShouldReturnJwtResponse() throws Exception {
        VerifyCodeRequest request = new VerifyCodeRequest();
        request.setUsername("demo");
        request.setCode("123456");

        when(userService.verify(eq("demo"), eq("123456"), anyString())).thenReturn("jwt-token");
        when(cookieManager.createCookie("jwt-token"))
                .thenReturn(ResponseCookie.from("AUTH_TOKEN", "jwt-token").httpOnly(true).path("/").build());

        mockMvc.perform(post("/verify")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("AUTH_TOKEN=jwt-token")))
                .andExpect(jsonPath("$.mfa", is(false)))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
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

        when(userService.registerUser(any(User.class), eq(Role.USER), anyString())).thenReturn(
                new RegistrationResult(saved, java.util.List.of("ABCD-EFGH")));
        when(totpManager.getUriForImage("mfa-secret")).thenReturn("data:image/png;base64,qr");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mfa", is(true)))
                .andExpect(jsonPath("$.secretImageUri", is("data:image/png;base64,qr")))
                .andExpect(jsonPath("$.recoveryCodes[0]", is("ABCD-EFGH")));
    }

    @Test
    void logoutShouldClearTheCookie() throws Exception {
        when(cookieManager.clearCookie())
                .thenReturn(ResponseCookie.from("AUTH_TOKEN", "").httpOnly(true).path("/").maxAge(0).build());

        mockMvc.perform(post("/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("AUTH_TOKEN=")));
    }
}
