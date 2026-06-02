package com.github.leoyakubov.twofactorauth.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "security.auth.rate-limit")
public class AuthRateLimitProperties {

    @Min(1)
    private int maxAttempts;

    private Duration window;

    private Duration lockout;
}
