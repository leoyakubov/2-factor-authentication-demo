package com.github.leoyakubov.twofactorauth.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtConfigProperties(
        @NotBlank String header,
        @NotBlank String prefix,
        @NotNull Duration expiration,
        @NotBlank @Size(min = 32) String secret,
        @NotBlank String cookieName,
        @NotBlank String cookiePath,
        boolean cookieSecure,
        @NotBlank String cookieSameSite
) {
}
