package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.config.properties.AuthRateLimitProperties;
import com.github.leoyakubov.twofactorauth.exception.TooManyRequestsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class AuthAttemptService {

    enum AuthAttemptAction {
        SIGN_IN,
        VERIFY,
        SIGN_UP
    }

    private final AuthRateLimitProperties properties;
    private final Clock clock = Clock.systemUTC();
    private final Map<String, AttemptState> states = new ConcurrentHashMap<>();

    public AuthAttemptService(AuthRateLimitProperties properties) {
        this.properties = properties;
    }

    public void assertAllowed(AuthAttemptAction action, String identifier, String clientIp) {
        String key = key(action, identifier, clientIp);
        AttemptState state = states.get(key);
        if (state == null) {
            return;
        }

        synchronized (state) {
            purgeExpiredAttempts(state);
            Instant now = Instant.now(clock);
            if (state.lockedUntil != null && state.lockedUntil.isAfter(now)) {
                Duration remaining = Duration.between(now, state.lockedUntil);
                throw new TooManyRequestsException(String.format(
                        "Too many attempts. Try again in about %d minute(s) and %d second(s).",
                        Math.max(0, remaining.toMinutes()),
                        Math.max(0, remaining.minusMinutes(remaining.toMinutes()).getSeconds())));
            }
        }
    }

    public void recordSuccess(AuthAttemptAction action, String identifier, String clientIp) {
        states.remove(key(action, identifier, clientIp));
    }

    public void recordFailure(AuthAttemptAction action, String identifier, String clientIp) {
        String key = key(action, identifier, clientIp);
        AttemptState state = states.computeIfAbsent(key, unused -> new AttemptState());
        synchronized (state) {
            purgeExpiredAttempts(state);
            state.failures.addLast(Instant.now(clock));
            if (state.failures.size() >= properties.maxAttempts()) {
                state.lockedUntil = Instant.now(clock).plus(properties.lockout());
                state.failures.clear();
                log.warn("rate limit triggered for {}", key);
            }
        }
    }

    private void purgeExpiredAttempts(AttemptState state) {
        Instant threshold = Instant.now(clock).minus(properties.window());
        while (!state.failures.isEmpty() && state.failures.peekFirst().isBefore(threshold)) {
            state.failures.removeFirst();
        }

        if (state.lockedUntil != null && state.lockedUntil.isBefore(Instant.now(clock))) {
            state.lockedUntil = null;
        }
    }

    private String key(AuthAttemptAction action, String identifier, String clientIp) {
        return String.format("%s:%s:%s",
                action == null ? "" : action.name().toLowerCase(Locale.ROOT),
                identifier == null ? "" : identifier.toLowerCase(Locale.ROOT),
                clientIp == null ? "unknown" : clientIp);
    }

    private static class AttemptState {
        private final Deque<Instant> failures = new ArrayDeque<>();
        private Instant lockedUntil;
    }
}
