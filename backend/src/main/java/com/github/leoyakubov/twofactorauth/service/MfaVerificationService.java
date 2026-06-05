package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.exception.BadRequestException;
import com.github.leoyakubov.twofactorauth.exception.InternalServerException;
import com.github.leoyakubov.twofactorauth.model.AuthUserDetails;
import com.github.leoyakubov.twofactorauth.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class MfaVerificationService {

    private final UserService userService;
    private final TotpService totpService;
    private final MfaSecretService mfaSecretService;
    private final RecoveryCodeService recoveryCodeService;
    private final JwtTokenService jwtTokenService;
    private final AuthAttemptService authAttemptService;

    public MfaVerificationService(UserService userService,
                                  TotpService totpService,
                                  MfaSecretService mfaSecretService,
                                  RecoveryCodeService recoveryCodeService,
                                  JwtTokenService jwtTokenService,
                                  AuthAttemptService authAttemptService) {
        this.userService = userService;
        this.totpService = totpService;
        this.mfaSecretService = mfaSecretService;
        this.recoveryCodeService = recoveryCodeService;
        this.jwtTokenService = jwtTokenService;
        this.authAttemptService = authAttemptService;
    }

    public String verify(String username, String code, String clientIp) {
        authAttemptService.assertAllowed(AuthAttemptService.AuthAttemptAction.VERIFY, username, clientIp);
        try {
            User user = userService.getByUsername(username);
            String mfaSecret = mfaSecretService.decrypt(user.getSecret());
            boolean totpCodeAccepted = totpService.verifyCode(code, mfaSecret);
            boolean recoveryCodeAccepted = !totpCodeAccepted && recoveryCodeService.consumeRecoveryCode(user, code);

            if (!totpCodeAccepted && !recoveryCodeAccepted) {
                authAttemptService.recordFailure(AuthAttemptService.AuthAttemptAction.VERIFY, username, clientIp);
                log.warn("MFA verification failed");
                throw new BadRequestException("Code is incorrect");
            }

            log.debug("MFA verification succeeded for {} using {}", username,
                    recoveryCodeAccepted ? "recovery code" : "authenticator app code");
            authAttemptService.recordSuccess(AuthAttemptService.AuthAttemptAction.VERIFY, username, clientIp);
            userService.save(user);
            return Optional.of(user)
                    .map(AuthUserDetails::new)
                    .map(userDetails -> new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()))
                    .map(jwtTokenService::generateToken)
                    .orElseThrow(() ->
                            new InternalServerException("unable to generate access token"));
        } catch (RuntimeException ex) {
            if (!(ex instanceof BadRequestException)) {
                authAttemptService.recordFailure(AuthAttemptService.AuthAttemptAction.VERIFY, username, clientIp);
            }
            throw ex;
        }
    }
}
