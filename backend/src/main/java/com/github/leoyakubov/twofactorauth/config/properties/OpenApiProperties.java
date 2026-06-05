package com.github.leoyakubov.twofactorauth.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.openapi")
public record OpenApiProperties(
        @NotBlank String title,
        @NotBlank String version,
        @NotBlank String description,
        @NotBlank String license
) {
}
