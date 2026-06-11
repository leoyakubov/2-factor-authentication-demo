package com.github.leoyakubov.twofactorauth.controller;

import com.github.leoyakubov.twofactorauth.model.AuthUserDetails;
import com.github.leoyakubov.twofactorauth.payload.UserSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Tag(name = "Users", description = "Authenticated user profile endpoints")
public class UserController {

    private static final String ME_PATH = "/users/me";

    @GetMapping(value = ME_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get the current authenticated user profile")
    public UserSummary getCurrentUser(@AuthenticationPrincipal AuthUserDetails userDetails) {
        log.debug("retrieving profile for authenticated user");
        return new UserSummary(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getUserProfile().getDisplayName(),
                userDetails.getUserProfile().getProfilePictureUrl(),
                userDetails.isMfa());
    }
}
