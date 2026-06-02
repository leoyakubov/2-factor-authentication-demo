package com.github.leoyakubov.twofactorauth.payload;

import com.github.leoyakubov.twofactorauth.model.User;

import java.util.List;

public record RegistrationResult(User user, List<String> recoveryCodes) {
}
