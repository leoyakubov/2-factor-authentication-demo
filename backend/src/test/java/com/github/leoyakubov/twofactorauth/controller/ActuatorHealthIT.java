package com.github.leoyakubov.twofactorauth.controller;

import com.github.leoyakubov.twofactorauth.controller.routes.ApiRoutes;
import com.github.leoyakubov.twofactorauth.repository.UserRepository;
import com.github.leoyakubov.twofactorauth.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import jakarta.servlet.Filter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration,org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration,org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration,de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration",
        "security.jwt.secret=01234567890123456789012345678901"
})
class ActuatorHealthIT {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthenticationService authenticationService;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext)
                .addFilters(webApplicationContext.getBean("springSecurityFilterChain", Filter.class))
                .build();
    }

    @Test
    void shouldReturnUpForActuatorHealthEndpoint() throws Exception {
        mockMvc.perform(get(ApiRoutes.ACTUATOR_HEALTH_PATH))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"UP\"}"));
    }
}
