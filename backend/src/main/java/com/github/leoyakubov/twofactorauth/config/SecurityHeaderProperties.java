package com.github.leoyakubov.twofactorauth.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.headers")
public record SecurityHeaderProperties(
        @NotBlank String contentSecurityPolicy
) {
}
