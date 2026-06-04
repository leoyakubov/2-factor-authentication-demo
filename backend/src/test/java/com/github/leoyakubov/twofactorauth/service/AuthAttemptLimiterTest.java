package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.config.AuthRateLimitProperties;
import com.github.leoyakubov.twofactorauth.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthAttemptLimiterTest {

    @Test
    void shouldAllowRequestsUntilLimitIsReached() {
        AuthRateLimitProperties properties = new AuthRateLimitProperties(
                2,
                Duration.ofMinutes(10),
                Duration.ofMinutes(15)
        );

        AuthAttemptLimiter limiter = new AuthAttemptLimiter(properties);

        assertDoesNotThrow(() -> limiter.assertAllowed("signin", "demo", "127.0.0.1"));
        limiter.recordFailure("signin", "demo", "127.0.0.1");
        assertDoesNotThrow(() -> limiter.assertAllowed("signin", "demo", "127.0.0.1"));
        limiter.recordFailure("signin", "demo", "127.0.0.1");
        assertThrows(TooManyRequestsException.class,
                () -> limiter.assertAllowed("signin", "demo", "127.0.0.1"));
    }

    @Test
    void successShouldClearFailureState() {
        AuthRateLimitProperties properties = new AuthRateLimitProperties(
                2,
                Duration.ofMinutes(10),
                Duration.ofMinutes(15)
        );

        AuthAttemptLimiter limiter = new AuthAttemptLimiter(properties);
        limiter.recordFailure("verify", "demo", "127.0.0.1");
        limiter.recordSuccess("verify", "demo", "127.0.0.1");

        assertDoesNotThrow(() -> limiter.assertAllowed("verify", "demo", "127.0.0.1"));
    }
}
