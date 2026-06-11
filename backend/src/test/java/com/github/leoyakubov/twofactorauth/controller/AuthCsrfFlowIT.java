package com.github.leoyakubov.twofactorauth.controller;

import com.github.leoyakubov.twofactorauth.controller.routes.ApiRoutes;
import com.github.leoyakubov.twofactorauth.model.AuthUserDetails;
import com.github.leoyakubov.twofactorauth.model.Profile;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import com.github.leoyakubov.twofactorauth.payload.LoginResult;
import com.github.leoyakubov.twofactorauth.repository.UserRepository;
import com.github.leoyakubov.twofactorauth.service.AuthenticationService;
import com.github.leoyakubov.twofactorauth.service.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration,org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration,org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration,de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration",
        "security.jwt.secret=01234567890123456789012345678901"
})
class AuthCsrfFlowIT {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Autowired
    private JwtTokenService jwtTokenService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext)
                .addFilters(webApplicationContext.getBean("springSecurityFilterChain", Filter.class))
                .build();
    }

    @Test
    void shouldBootstrapCsrfTokenAndAllowSigninWithIt() throws Exception {
        when(authenticationService.login(eq("demo"), eq("secret"), anyString()))
                .thenReturn(LoginResult.authenticated("jwt-token"));

        MvcResult csrfResult = mockMvc.perform(get(ApiRoutes.CSRF_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn();

        String csrfToken = objectMapper.readTree(csrfResult.getResponse().getContentAsString()).get("token").asText();
        Cookie csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");

        assertThat(csrfToken).isNotBlank();
        assertThat(csrfCookie).isNotNull();

        mockMvc.perform(post(ApiRoutes.SIGNIN_PATH)
                        .cookie(csrfCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-XSRF-TOKEN", csrfToken)
                        .content(objectMapper.writeValueAsString(new DemoLoginRequest("demo", "secret"))))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("AUTH_TOKEN"))
                .andExpect(jsonPath("$.mfa").value(false));

        verify(authenticationService).login(eq("demo"), eq("secret"), anyString());
    }

    @Test
    void shouldReturnNoContentAndClearAuthCookieOnLogout() throws Exception {
        MvcResult csrfResult = mockMvc.perform(get(ApiRoutes.CSRF_PATH))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn();

        String csrfToken = objectMapper.readTree(csrfResult.getResponse().getContentAsString()).get("token").asText();
        Cookie csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");

        mockMvc.perform(post(ApiRoutes.LOGOUT_PATH)
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfToken))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("AUTH_TOKEN", 0));
    }

    @Test
    void shouldRejectProfileRequestWithoutAuthentication() throws Exception {
        mockMvc.perform(get(ApiRoutes.USERS_PATH + "/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnCurrentProfileWhenJwtCookieMatchesRegisteredUser() throws Exception {
        User user = buildUser("demo", "Demo User");
        user.setId("user-1");
        AuthUserDetails userDetails = new AuthUserDetails(user);

        when(userRepository.findByUsername("demo")).thenReturn(java.util.Optional.of(user));
        String generatedToken = jwtTokenService.generateToken(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()));

        mockMvc.perform(get(ApiRoutes.USERS_PATH + "/me")
                        .cookie(new Cookie("AUTH_TOKEN", generatedToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-1"))
                .andExpect(jsonPath("$.username").value("demo"))
                .andExpect(jsonPath("$.email").value("demo@example.com"))
                .andExpect(jsonPath("$.name").value("Demo User"))
                .andExpect(jsonPath("$.mfaEnabled").value(false));
    }

    private static User buildUser(String username, String displayName) {
        return User.builder()
                .username(username)
                .email(username + "@example.com")
                .password("encoded")
                .active(true)
                .userProfile(Profile.builder()
                        .displayName(displayName)
                        .build())
                .roles(java.util.Set.of(Role.USER))
                .mfa(false)
                .build();
    }

    private record DemoLoginRequest(String username, String password) {
    }
}
