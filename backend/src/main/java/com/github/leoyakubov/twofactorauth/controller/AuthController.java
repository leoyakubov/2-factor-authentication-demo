package com.github.leoyakubov.twofactorauth.controller;

import com.github.leoyakubov.twofactorauth.config.JwtCookieManager;
import com.github.leoyakubov.twofactorauth.controller.routes.ApiRoutes;
import com.github.leoyakubov.twofactorauth.model.Profile;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import com.github.leoyakubov.twofactorauth.payload.JwtAuthenticationResponse;
import com.github.leoyakubov.twofactorauth.payload.LoginRequest;
import com.github.leoyakubov.twofactorauth.payload.LoginResult;
import com.github.leoyakubov.twofactorauth.payload.RegistrationResult;
import com.github.leoyakubov.twofactorauth.payload.SignUpRequest;
import com.github.leoyakubov.twofactorauth.payload.SignupResponse;
import com.github.leoyakubov.twofactorauth.payload.VerifyCodeRequest;
import com.github.leoyakubov.twofactorauth.service.TotpService;
import com.github.leoyakubov.twofactorauth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
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
public class AuthController {
    private static final String USERS_WITH_USERNAME_PATH = "/users/{username}";

    private final UserService userService;
    private final TotpService totpService;
    private final JwtCookieManager cookieManager;

    public AuthController(UserService userService, TotpService totpService, JwtCookieManager cookieManager) {
        this.userService = userService;
        this.totpService = totpService;
        this.cookieManager = cookieManager;
    }

    @PostMapping(ApiRoutes.SIGNIN_PATH)
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                                                       HttpServletRequest request) {
        log.info("sign-in attempt for {} from {}", loginRequest.username(), request.getRemoteAddr());
        LoginResult result = userService.loginUser(loginRequest.username(), loginRequest.password(),
                request.getRemoteAddr());
        log.info("sign-in completed for {} from {} (mfa={})", loginRequest.username(), request.getRemoteAddr(), result.mfaRequired());
        if (result.mfaRequired()) {
            return ResponseEntity.ok(new JwtAuthenticationResponse(true));
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.createCookie(result.accessToken()).toString())
                .body(new JwtAuthenticationResponse(false));
    }

    @PostMapping(ApiRoutes.VERIFY_PATH)
    public ResponseEntity<JwtAuthenticationResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest verifyCodeRequest,
                                                                 HttpServletRequest request) {
        log.info("mfa verify attempt for {} from {}", verifyCodeRequest.username(), request.getRemoteAddr());
        String token = userService.verify(verifyCodeRequest.username(), verifyCodeRequest.code(),
                request.getRemoteAddr());
        log.info("mfa verify completed for {} from {}", verifyCodeRequest.username(), request.getRemoteAddr());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.createCookie(token).toString())
                .body(new JwtAuthenticationResponse(false));
    }

    @PostMapping(value = ApiRoutes.USERS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SignupResponse> createUser(@Valid @RequestBody SignUpRequest payload,
                                                     HttpServletRequest request) {
        log.info("creating user {} from {}", payload.username(), request.getRemoteAddr());
        RegistrationResult registrationResult = userService.registerUser(toUser(payload), Role.USER,
                request.getRemoteAddr());

        User saved = registrationResult.user();
        log.info("user created {} from {} (mfa={})", saved.getUsername(), request.getRemoteAddr(), saved.isMfa());

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path(USERS_WITH_USERNAME_PATH)
                .buildAndExpand(saved.getUsername()).toUri();

        return ResponseEntity
                .created(location)
                .body(toSignupResponse(saved, registrationResult));
    }

    @PostMapping(ApiRoutes.LOGOUT_PATH)
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        log.info("logout requested from {}", request.getRemoteAddr());
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieManager.clearCookie().toString())
                .build();
    }

    @GetMapping(ApiRoutes.CSRF_PATH)
    public ResponseEntity<Void> csrf(CsrfToken token) {
        return ResponseEntity.noContent().build();
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
                .mfa(payload.mfa())
                .build();
    }

    private SignupResponse toSignupResponse(User saved, RegistrationResult registrationResult) {
        return new SignupResponse(
                saved.isMfa(),
                saved.isMfa() ? totpService.getUriForImage(saved.getSecret()) : null,
                registrationResult.recoveryCodes());
    }
}
