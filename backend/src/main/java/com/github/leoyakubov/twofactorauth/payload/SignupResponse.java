package com.github.leoyakubov.twofactorauth.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SignupResponse {
    private boolean mfa;
    private String secretImageUri;
    private List<String> recoveryCodes;
}
