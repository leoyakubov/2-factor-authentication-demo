package com.github.leoyakubov.twofactorauth.payload;

import java.util.List;

public record SignupResponse(boolean mfa, String secretImageUri, List<String> recoveryCodes) {
}
