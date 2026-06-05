package com.github.leoyakubov.twofactorauth.controller;

import com.github.leoyakubov.twofactorauth.controller.routes.ApiRoutes;
import com.github.leoyakubov.twofactorauth.payload.LoginResult;
import com.github.leoyakubov.twofactorauth.repository.UserRepository;
import com.github.leoyakubov.twofactorauth.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.Cookie;
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

    private record DemoLoginRequest(String username, String password) {
    }
}
