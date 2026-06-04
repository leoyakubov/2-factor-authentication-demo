package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.model.AuthUserDetails;
import com.github.leoyakubov.twofactorauth.payload.LoginResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final AuthAttemptService authAttemptService;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                 JwtTokenService jwtTokenService,
                                 AuthAttemptService authAttemptService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.authAttemptService = authAttemptService;
    }

    public LoginResult login(String username, String password, String clientIp) {
        authAttemptService.assertAllowed(AuthAttemptService.AuthAttemptAction.SIGN_IN, username, clientIp);
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(username, password));

            AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();
            if (userDetails.isMfa()) {
                log.info("login accepted for {} and MFA verification is required", userDetails.getUsername());
                authAttemptService.recordSuccess(AuthAttemptService.AuthAttemptAction.SIGN_IN, username, clientIp);
                return LoginResult.requiresMfa();
            }

            Authentication canonicalAuthentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            log.info("login accepted for {} and issuing access token", userDetails.getUsername());
            authAttemptService.recordSuccess(AuthAttemptService.AuthAttemptAction.SIGN_IN, username, clientIp);
            return LoginResult.authenticated(jwtTokenService.generateToken(canonicalAuthentication));
        } catch (RuntimeException ex) {
            authAttemptService.recordFailure(AuthAttemptService.AuthAttemptAction.SIGN_IN, username, clientIp);
            throw ex;
        }
    }
}
