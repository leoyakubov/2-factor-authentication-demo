package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.exception.EmailAlreadyExistsException;
import com.github.leoyakubov.twofactorauth.exception.UsernameAlreadyExistsException;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import com.github.leoyakubov.twofactorauth.payload.RegistrationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class RegistrationService {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final TotpService totpService;
    private final MfaSecretService mfaSecretService;
    private final RecoveryCodeService recoveryCodeService;
    private final AuthAttemptService authAttemptService;

    public RegistrationService(PasswordEncoder passwordEncoder,
                               UserService userService,
                               TotpService totpService,
                               MfaSecretService mfaSecretService,
                               RecoveryCodeService recoveryCodeService,
                               AuthAttemptService authAttemptService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.totpService = totpService;
        this.mfaSecretService = mfaSecretService;
        this.recoveryCodeService = recoveryCodeService;
        this.authAttemptService = authAttemptService;
    }

    public RegistrationResult register(User user, Role role, String clientIp) {
        log.info("registering user {}", user.getUsername());
        authAttemptService.assertAllowed(AuthAttemptService.AuthAttemptAction.SIGN_UP, user.getUsername(), clientIp);

        if (userService.existsByUsername(user.getUsername())) {
            log.warn("username {} already exists.", user.getUsername());
            authAttemptService.recordFailure(AuthAttemptService.AuthAttemptAction.SIGN_UP, user.getUsername(), clientIp);
            throw new UsernameAlreadyExistsException(
                    String.format("username %s already exists", user.getUsername()));
        }

        if (userService.existsByEmail(user.getEmail())) {
            log.warn("email {} already exists.", user.getEmail());
            authAttemptService.recordFailure(AuthAttemptService.AuthAttemptAction.SIGN_UP, user.getUsername(), clientIp);
            throw new EmailAlreadyExistsException(
                    String.format("email %s already exists", user.getEmail()));
        }

        user.setActive(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(new HashSet<>(Set.of(role)));

        if (user.isMfa()) {
            String mfaSecret = totpService.generateSecret();
            user.setSecret(mfaSecretService.encrypt(mfaSecret));
            List<String> recoveryCodes = recoveryCodeService.generateRecoveryCodes();
            user.setRecoveryCodes(recoveryCodeService.hashCodes(recoveryCodes));
            log.info("generated {} recovery codes for {}", recoveryCodes.size(), user.getUsername());
            authAttemptService.recordSuccess(AuthAttemptService.AuthAttemptAction.SIGN_UP, user.getUsername(), clientIp);
            log.info("saved user {} (mfa={})", user.getUsername(), user.isMfa());
            return new RegistrationResult(userService.save(user), mfaSecret, recoveryCodes);
        }

        log.info("saved user {} (mfa={})", user.getUsername(), user.isMfa());
        authAttemptService.recordSuccess(AuthAttemptService.AuthAttemptAction.SIGN_UP, user.getUsername(), clientIp);
        return new RegistrationResult(userService.save(user), null, List.of());
    }
}
