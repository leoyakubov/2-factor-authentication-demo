package com.github.leoyakubov.twofactorauth.controller;

import com.github.leoyakubov.twofactorauth.model.AuthUserDetails;
import com.github.leoyakubov.twofactorauth.payload.UserSummary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private static final String ME_PATH = "/users/me";

    @GetMapping(value = ME_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    public UserSummary getCurrentUser(@AuthenticationPrincipal AuthUserDetails userDetails) {
        return new UserSummary(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getUserProfile().getDisplayName(),
                userDetails.getUserProfile().getProfilePictureUrl());
    }
}
