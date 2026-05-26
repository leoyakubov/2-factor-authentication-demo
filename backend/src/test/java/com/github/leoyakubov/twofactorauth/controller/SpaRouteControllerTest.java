package com.github.leoyakubov.twofactorauth.controller;

import com.github.leoyakubov.twofactorauth.config.FrontendProperties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpaRouteController.class)
@AutoConfigureMockMvc(addFilters = false)
class SpaRouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FrontendProperties frontendProperties;

    @ParameterizedTest
    @CsvSource({
            "'/','http://localhost:3000/'",
            "'/login','http://localhost:3000/login'",
            "'/signup','http://localhost:3000/signup'",
            "'/verify','http://localhost:3000/verify'",
            "'/qrcode','http://localhost:3000/qrcode'"
    })
    void spaRoutesShouldRedirectToFrontend(String path, String expectedRedirect) throws Exception {
        when(frontendProperties.getBaseUrl()).thenReturn("http://localhost:3000");

        mockMvc.perform(get(path))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(expectedRedirect));
    }

    @ParameterizedTest
    @CsvSource({
            "'/login','next=profile','http://localhost:3000/login?next=profile'",
            "'/verify','foo=bar&baz=qux','http://localhost:3000/verify?foo=bar&baz=qux'"
    })
    void spaRouteRedirectShouldPreserveQueryString(String path,
                                                   String queryString,
                                                   String expectedRedirect) throws Exception {
        when(frontendProperties.getBaseUrl()).thenReturn("http://localhost:3000");

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
