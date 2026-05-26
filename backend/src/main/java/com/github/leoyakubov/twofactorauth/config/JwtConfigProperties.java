package com.github.leoyakubov.twofactorauth.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public class JwtConfigProperties {

    @NotBlank
    private String header;

    @NotBlank
    private String prefix;

    @Min(1)
    private int expiration;

    @NotBlank
    private String secret;
}
