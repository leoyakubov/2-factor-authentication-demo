package com.github.leoyakubov.twofactorauth.config;

import com.github.leoyakubov.twofactorauth.config.properties.OpenApiProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    public OpenAPI twoFactorAuthOpenApi(OpenApiProperties openApiProperties) {
        return new OpenAPI()
                .info(new Info()
                        .title(openApiProperties.title())
                        .version(openApiProperties.version())
                        .description(openApiProperties.description())
                        .license(new License().name(openApiProperties.license())));
    }
}
