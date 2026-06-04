package com.github.leoyakubov.twofactorauth.payload;

public record VerifyCodeRequest(String username, String code) {
}
