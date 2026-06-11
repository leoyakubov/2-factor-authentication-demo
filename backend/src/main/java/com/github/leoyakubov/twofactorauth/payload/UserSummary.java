package com.github.leoyakubov.twofactorauth.payload;

public record UserSummary(String id, String username, String email, String name, String profilePicture,
                          boolean mfaEnabled) {
}
