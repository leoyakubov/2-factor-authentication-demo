package com.github.leoyakubov.twofactorauth.payload;

public record LoginResult(boolean mfaRequired, String accessToken) {

    public static LoginResult mfaRequired() {
        return new LoginResult(true, null);
    }

    public static LoginResult authenticated(String accessToken) {
        return new LoginResult(false, accessToken);
    }
}
