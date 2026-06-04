package com.github.leoyakubov.twofactorauth.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Validated
@ConfigurationProperties(prefix = "security.headers")
public record SecurityHeaderProperties(
        String contentSecurityPolicyOverride,
        @Valid @NotNull ContentSecurityPolicy contentSecurityPolicy
) {

    public String contentSecurityPolicyHeaderValue() {
        if (StringUtils.hasText(contentSecurityPolicyOverride)) {
            return contentSecurityPolicyOverride;
        }

        return contentSecurityPolicy.toHeaderValue();
    }

    public record ContentSecurityPolicy(
            @NotEmpty List<String> defaultSrc,
            @NotEmpty List<String> baseUri,
            @NotEmpty List<String> formAction,
            @NotEmpty List<String> frameAncestors,
            @NotEmpty List<String> imgSrc,
            @NotEmpty List<String> scriptSrc,
            @NotEmpty List<String> styleSrc,
            @NotEmpty List<String> connectSrc
    ) {

        private String toHeaderValue() {
            return Stream.of(
                            directive("default-src", defaultSrc),
                            directive("base-uri", baseUri),
                            directive("form-action", formAction),
                            directive("frame-ancestors", frameAncestors),
                            directive("img-src", imgSrc),
                            directive("script-src", scriptSrc),
                            directive("style-src", styleSrc),
                            directive("connect-src", connectSrc)
                    )
                    .collect(Collectors.joining("; "));
        }

        private String directive(String name, List<String> values) {
            return name + " " + String.join(" ", values);
        }
    }
}
