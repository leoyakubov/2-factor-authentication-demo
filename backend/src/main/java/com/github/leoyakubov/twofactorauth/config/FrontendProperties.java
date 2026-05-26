package com.github.leoyakubov.twofactorauth.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.frontend")
public class FrontendProperties {

    @NotBlank
    private String baseUrl;
}
