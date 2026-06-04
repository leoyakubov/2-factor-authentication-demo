package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.exception.BadRequestException;
import com.github.leoyakubov.twofactorauth.exception.EmailAlreadyExistsException;
import com.github.leoyakubov.twofactorauth.exception.InternalServerException;
import com.github.leoyakubov.twofactorauth.exception.ResourceNotFoundException;
import com.github.leoyakubov.twofactorauth.exception.UsernameAlreadyExistsException;
import com.github.leoyakubov.twofactorauth.model.AuthUserDetails;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import com.github.leoyakubov.twofactorauth.payload.LoginResult;
import com.github.leoyakubov.twofactorauth.payload.RegistrationResult;
import com.github.leoyakubov.twofactorauth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final TotpService totpService;
    private final RecoveryCodeService recoveryCodeService;
    private final AuthAttemptService authAttemptService;

    public UserService(@Lazy PasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       @Lazy AuthenticationManager authenticationManager,
                       JwtTokenService jwtTokenService,
                       TotpService totpService,
                       RecoveryCodeService recoveryCodeService,
                       AuthAttemptService authAttemptService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.totpService = totpService;
        this.recoveryCodeService = recoveryCodeService;
        this.authAttemptService = authAttemptService;
    }

    public LoginResult loginUser(String username, String password) {
        return loginUser(username, password, "unknown");
    }

    public LoginResult loginUser(String username, String password, String clientIp) {
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

    public String verify(String username, String code) {
        return verify(username, code, "unknown");
    }

    public String verify(String username, String code, String clientIp) {
        authAttemptService.assertAllowed(AuthAttemptService.AuthAttemptAction.VERIFY, username, clientIp);
        try {
            User user = userRepository
                    .findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException( String.format("username %s", username)));

            if (!totpService.verifyCode(code, user.getSecret()) && !recoveryCodeService.consumeRecoveryCode(user, code)) {
                authAttemptService.recordFailure(AuthAttemptService.AuthAttemptAction.VERIFY, username, clientIp);
                log.warn("MFA verification failed for {}", username);
                throw new BadRequestException("Code is incorrect");
            }

            log.info("MFA verification succeeded for {}", username);
            authAttemptService.recordSuccess(AuthAttemptService.AuthAttemptAction.VERIFY, username, clientIp);
            userRepository.save(user);
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

    public User registerUser(User user, Role role) {
        return registerUser(user, role, "unknown").user();
    }

    public RegistrationResult registerUser(User user, Role role, String clientIp) {
        log.info("registering user {}", user.getUsername());
        authAttemptService.assertAllowed(AuthAttemptService.AuthAttemptAction.SIGN_UP, user.getUsername(), clientIp);

        if (userRepository.existsByUsername(user.getUsername())) {
            log.warn("username {} already exists.", user.getUsername());
            authAttemptService.recordFailure(AuthAttemptService.AuthAttemptAction.SIGN_UP, user.getUsername(), clientIp);

            throw new UsernameAlreadyExistsException(
                    String.format("username %s already exists", user.getUsername()));
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("email {} already exists.", user.getEmail());
            authAttemptService.recordFailure(AuthAttemptService.AuthAttemptAction.SIGN_UP, user.getUsername(), clientIp);

            throw new EmailAlreadyExistsException(
                    String.format("email %s already exists", user.getEmail()));
        }
        user.setActive(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(new HashSet<>(Set.of(role)));

        if (user.isMfa()) {
            user.setSecret(totpService.generateSecret());
            java.util.List<String> recoveryCodes = recoveryCodeService.generateRecoveryCodes();
            user.setRecoveryCodes(recoveryCodeService.hashCodes(recoveryCodes));
            log.info("generated {} recovery codes for {}", recoveryCodes.size(), user.getUsername());
            authAttemptService.recordSuccess(AuthAttemptService.AuthAttemptAction.SIGN_UP, user.getUsername(), clientIp);
            log.info("saved user {} (mfa={})", user.getUsername(), user.isMfa());
            return new RegistrationResult(userRepository.save(user), recoveryCodes);
        }

        log.info("saved user {} (mfa={})", user.getUsername(), user.isMfa());
        authAttemptService.recordSuccess(AuthAttemptService.AuthAttemptAction.SIGN_UP, user.getUsername(), clientIp);
        return new RegistrationResult(userRepository.save(user), java.util.List.of());
    }

    public Optional<User> findByUsername(String username) {
        log.info("retrieving user {}", username);
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByUsernameOrEmail(String identifier) {
        log.info("retrieving user {}", identifier);
        return userRepository.findByUsername(identifier).or(() -> userRepository.findByEmail(identifier));
    }
}
