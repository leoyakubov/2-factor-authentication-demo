package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.config.properties.AuthRateLimitProperties;
import com.github.leoyakubov.twofactorauth.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthAttemptServiceTest {

    @Test
    void shouldAllowRequestsUntilLimitIsReachedForTheSameKey() {
        AuthRateLimitProperties properties = new AuthRateLimitProperties(
                2,
                Duration.ofMinutes(10),
                Duration.ofMinutes(15)
        );

        AuthAttemptService service = new AuthAttemptService(properties);

        assertDoesNotThrow(() -> service.assertAllowed(AuthAttemptService.AuthAttemptAction.SIGN_IN, "demo", "127.0.0.1"));
        service.recordFailure(AuthAttemptService.AuthAttemptAction.SIGN_IN, "demo", "127.0.0.1");
        assertDoesNotThrow(() -> service.assertAllowed(AuthAttemptService.AuthAttemptAction.SIGN_IN, "demo", "127.0.0.1"));
        service.recordFailure(AuthAttemptService.AuthAttemptAction.SIGN_IN, "demo", "127.0.0.1");
        assertThrows(TooManyRequestsException.class,
                () -> service.assertAllowed(AuthAttemptService.AuthAttemptAction.SIGN_IN, "demo", "127.0.0.1"));
    }

    @Test
    void shouldClearFailureStateAfterSuccessfulRequest() {
        AuthRateLimitProperties properties = new AuthRateLimitProperties(
                2,
                Duration.ofMinutes(10),
                Duration.ofMinutes(15)
        );

        AuthAttemptService service = new AuthAttemptService(properties);
        service.recordFailure(AuthAttemptService.AuthAttemptAction.VERIFY, "demo", "127.0.0.1");
        service.recordSuccess(AuthAttemptService.AuthAttemptAction.VERIFY, "demo", "127.0.0.1");

        assertDoesNotThrow(() -> service.assertAllowed(AuthAttemptService.AuthAttemptAction.VERIFY, "demo", "127.0.0.1"));
    }
}
