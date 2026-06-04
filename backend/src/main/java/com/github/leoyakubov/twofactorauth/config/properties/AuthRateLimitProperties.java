package com.github.leoyakubov.twofactorauth.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "security.auth.rate-limit")
public record AuthRateLimitProperties(
        @Min(1) int maxAttempts,
        @NotNull Duration window,
        @NotNull Duration lockout
) {
}
