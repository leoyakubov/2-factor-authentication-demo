package com.github.leoyakubov.twofactorauth.controller;

import com.github.leoyakubov.twofactorauth.config.properties.FrontendProperties;
import com.github.leoyakubov.twofactorauth.controller.routes.ApiRoutes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SpaRouteControllerTest {

    private final FrontendProperties frontendProperties = new FrontendProperties("http://localhost:3000");

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SpaRouteController(frontendProperties)).build();
    }

    @ParameterizedTest
    @CsvSource({
            "'/','http://localhost:3000/'",
            "'" + ApiRoutes.LOGIN_PATH + "','http://localhost:3000/login'",
            "'" + ApiRoutes.SIGNUP_PATH + "','http://localhost:3000/signup'",
            "'" + ApiRoutes.VERIFY_PATH + "','http://localhost:3000/verify'",
            "'" + ApiRoutes.QRCODE_PATH + "','http://localhost:3000/qrcode'"
    })
    void shouldRedirectSpaRoutesToFrontend(String path, String expectedRedirect) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(expectedRedirect));
    }

    @ParameterizedTest
    @CsvSource({
            "'" + ApiRoutes.LOGIN_PATH + "','next=profile','http://localhost:3000/login?next=profile'",
            "'" + ApiRoutes.VERIFY_PATH + "','foo=bar&baz=qux','http://localhost:3000/verify?foo=bar&baz=qux'"
    })
    void shouldPreserveQueryStringWhenRedirectingSpaRoute(String path,
                                                          String queryString,
                                                          String expectedRedirect) throws Exception {
        String[] params = queryString.split("&");
        var requestBuilder = get(path);
        for (String param : params) {
            String[] pair = param.split("=");
            requestBuilder.param(pair[0], pair[1]);
        }

        mockMvc.perform(requestBuilder)
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(expectedRedirect));
    }
}
