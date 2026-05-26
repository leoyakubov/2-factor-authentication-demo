package com.github.leoyakubov.twofactorauth.controller;

import com.github.leoyakubov.twofactorauth.exception.BadRequestException;
import com.github.leoyakubov.twofactorauth.exception.EmailAlreadyExistsException;
import com.github.leoyakubov.twofactorauth.exception.UsernameAlreadyExistsException;
import com.github.leoyakubov.twofactorauth.config.JwtCookieManager;
import com.github.leoyakubov.twofactorauth.model.Profile;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import com.github.leoyakubov.twofactorauth.payload.JwtAuthenticationResponse;
import com.github.leoyakubov.twofactorauth.payload.LoginResult;
import com.github.leoyakubov.twofactorauth.payload.LoginRequest;
import com.github.leoyakubov.twofactorauth.payload.SignUpRequest;
import com.github.leoyakubov.twofactorauth.payload.SignupResponse;
import com.github.leoyakubov.twofactorauth.payload.VerifyCodeRequest;
import com.github.leoyakubov.twofactorauth.service.TotpManager;
import com.github.leoyakubov.twofactorauth.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@Slf4j
public class AuthController {

    private final UserService userService;
    private final TotpManager totpManager;
    private final JwtCookieManager cookieManager;

    public AuthController(UserService userService, TotpManager totpManager, JwtCookieManager cookieManager) {
        this.userService = userService;
        this.totpManager = totpManager;
        this.cookieManager = cookieManager;
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("sign-in attempt for {}", loginRequest.getUsername());
        LoginResult result = userService.loginUser(loginRequest.getUsername(), loginRequest.getPassword());
        log.info("sign-in completed for {} (mfa={})", loginRequest.getUsername(), result.mfaRequired());
        if (result.mfaRequired()) {
            return ResponseEntity.ok(new JwtAuthenticationResponse(true));
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.createCookie(result.accessToken()).toString())
                .body(new JwtAuthenticationResponse(false));
    }

    @PostMapping("/verify")
    public ResponseEntity<JwtAuthenticationResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest verifyCodeRequest) {
        log.info("mfa verify attempt for {}", verifyCodeRequest.getUsername());
        String token = userService.verify(verifyCodeRequest.getUsername(), verifyCodeRequest.getCode());
        log.info("mfa verify completed for {}", verifyCodeRequest.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.createCookie(token).toString())
                .body(new JwtAuthenticationResponse(false));
    }

    @PostMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SignupResponse> createUser(@Valid @RequestBody SignUpRequest payload) {
        log.info("creating user {}", payload.getUsername());

        User user = User
                .builder()
                .username(payload.getUsername())
                .email(payload.getEmail())
                .password(payload.getPassword())
                .userProfile(Profile
                        .builder()
                        .displayName(payload.getName())
                        .build())
                .mfa(payload.isMfa())
                .build();

        User saved;
        try {
             saved = userService.registerUser(user, Role.USER);
        } catch (UsernameAlreadyExistsException | EmailAlreadyExistsException e) {
            log.warn("signup rejected for {}: {}", payload.getUsername(), e.getMessage());
            throw new BadRequestException(e.getMessage());
        }

        log.info("user created {} (mfa={})", saved.getUsername(), saved.isMfa());

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{username}")
                .buildAndExpand(user.getUsername()).toUri();

        return ResponseEntity
                .created(location)
                .body(new SignupResponse(
                        saved.isMfa(),
                        saved.isMfa() ? totpManager.getUriForImage(saved.getSecret()) : null));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        log.info("logout requested");
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieManager.clearCookie().toString())
                .build();
    }
}
