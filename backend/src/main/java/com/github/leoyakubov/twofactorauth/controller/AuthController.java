package com.github.leoyakubov.twofactorauth.controller;

import com.github.leoyakubov.twofactorauth.config.JwtCookieManager;
import com.github.leoyakubov.twofactorauth.controller.routes.ApiRoutes;
import com.github.leoyakubov.twofactorauth.model.Profile;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import com.github.leoyakubov.twofactorauth.payload.JwtAuthenticationResponse;
import com.github.leoyakubov.twofactorauth.payload.LoginRequest;
import com.github.leoyakubov.twofactorauth.payload.LoginResult;
import com.github.leoyakubov.twofactorauth.payload.CsrfTokenResponse;
import com.github.leoyakubov.twofactorauth.payload.RegistrationResult;
import com.github.leoyakubov.twofactorauth.payload.SignUpRequest;
import com.github.leoyakubov.twofactorauth.payload.SignupResponse;
import com.github.leoyakubov.twofactorauth.payload.VerifyCodeRequest;
import com.github.leoyakubov.twofactorauth.service.AuthenticationService;
import com.github.leoyakubov.twofactorauth.service.MfaVerificationService;
import com.github.leoyakubov.twofactorauth.service.RegistrationService;
import com.github.leoyakubov.twofactorauth.service.TotpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@Slf4j
@Tag(name = "Authentication", description = "Signup, signin, MFA verification, and CSRF bootstrap")
public class AuthController {
    private static final String USERS_WITH_USERNAME_PATH = "/users/{username}";

    private final AuthenticationService authenticationService;
    private final RegistrationService registrationService;
    private final MfaVerificationService mfaVerificationService;
    private final TotpService totpService;
    private final JwtCookieManager cookieManager;

    public AuthController(AuthenticationService authenticationService,
                          RegistrationService registrationService,
                          MfaVerificationService mfaVerificationService,
                          TotpService totpService,
                          JwtCookieManager cookieManager) {
        this.authenticationService = authenticationService;
        this.registrationService = registrationService;
        this.mfaVerificationService = mfaVerificationService;
        this.totpService = totpService;
        this.cookieManager = cookieManager;
    }

    @PostMapping(ApiRoutes.SIGNIN_PATH)
    @Operation(summary = "Sign in with username or email and password")
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                                                       HttpServletRequest request) {
        log.debug("sign-in attempt received from {}", request.getRemoteAddr());
        LoginResult result = authenticationService.login(loginRequest.username(), loginRequest.password(),
                request.getRemoteAddr());
        log.debug("sign-in completed from {} (mfa={})", request.getRemoteAddr(), result.mfaRequired());
        if (result.mfaRequired()) {
            return ResponseEntity.ok(new JwtAuthenticationResponse(true));
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.createCookie(result.accessToken()).toString())
                .body(new JwtAuthenticationResponse(false));
    }

    @PostMapping(ApiRoutes.VERIFY_PATH)
    @Operation(summary = "Verify an MFA authenticator code or one-time recovery code")
    public ResponseEntity<JwtAuthenticationResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest verifyCodeRequest,
                                                                 HttpServletRequest request) {
        log.debug("mfa verify attempt received from {}", request.getRemoteAddr());
        String token = mfaVerificationService.verify(verifyCodeRequest.username(), verifyCodeRequest.code(),
                request.getRemoteAddr());
        log.debug("mfa verify completed from {}", request.getRemoteAddr());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.createCookie(token).toString())
                .body(new JwtAuthenticationResponse(false));
    }

    @PostMapping(value = ApiRoutes.USERS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a user with optional MFA enrollment")
    public ResponseEntity<SignupResponse> createUser(@Valid @RequestBody SignUpRequest payload,
                                                     HttpServletRequest request) {
        log.debug("creating user from {}", request.getRemoteAddr());
        RegistrationResult registrationResult = registrationService.register(toUser(payload), Role.USER,
                request.getRemoteAddr());

        User saved = registrationResult.user();
        log.debug("user created from {} (mfa={})", request.getRemoteAddr(), saved.isMfa());

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path(USERS_WITH_USERNAME_PATH)
                .buildAndExpand(saved.getUsername()).toUri();

        return ResponseEntity
                .created(location)
                .body(toSignupResponse(saved, registrationResult));
    }

    @GetMapping(ApiRoutes.CSRF_PATH)
    @Operation(summary = "Get a CSRF token for browser POST requests")
    public ResponseEntity<CsrfTokenResponse> csrf(CsrfToken token, HttpServletRequest request) {
        log.debug("csrf token requested from {}", request.getRemoteAddr());
        return ResponseEntity.ok(new CsrfTokenResponse(token.getToken()));
    }

    private User toUser(SignUpRequest payload) {
        return User
                .builder()
                .username(payload.username())
                .email(payload.email())
                .password(payload.password())
                .userProfile(Profile
                        .builder()
                        .displayName(payload.name())
                        .build())
                .mfa(Boolean.TRUE.equals(payload.mfa()))
                .build();
    }

    private SignupResponse toSignupResponse(User saved, RegistrationResult registrationResult) {
        return new SignupResponse(
                saved.isMfa(),
                saved.isMfa() ? totpService.getUriForImage(registrationResult.mfaSecret()) : null,
                registrationResult.recoveryCodes());
    }
}
