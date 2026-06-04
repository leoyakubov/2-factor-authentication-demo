package com.github.leoyakubov.twofactorauth.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.mfa")
public record MfaSecretProperties(
        @NotBlank @Size(min = 32) String encryptionKey
) {
}
