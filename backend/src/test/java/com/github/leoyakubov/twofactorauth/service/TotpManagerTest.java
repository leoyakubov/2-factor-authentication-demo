package com.github.leoyakubov.twofactorauth.service;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpManagerTest {

    private final TotpManager totpManager = new TotpManager();

    @Test
    void generateSecretShouldReturnValue() {
        assertNotNull(totpManager.generateSecret());
    }

    @Test
    void getUriForImageShouldReturnDataUri() {
        String uri = totpManager.getUriForImage("JBSWY3DPEHPK3PXP");

        assertTrue(uri.startsWith("data:image/png;base64,"));
    }

    @Test
    void getUriForImageShouldRejectBlankSecret() {
        assertThrows(IllegalArgumentException.class, () -> totpManager.getUriForImage(" "));
    }

    @Test
    void verifyCodeShouldAcceptCurrentCodeAndRejectWrongCode() throws Exception {
        String secret = totpManager.generateSecret();
        DefaultCodeGenerator codeGenerator = new DefaultCodeGenerator();
        long currentWindow = Math.floorDiv(new SystemTimeProvider().getTime(), 30L);
        String code = codeGenerator.generate(secret, currentWindow);

        assertTrue(totpManager.verifyCode(code, secret));
        assertFalse(totpManager.verifyCode("000000", secret));
    }
}
