package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.config.properties.MfaSecretProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MfaSecretServiceTest {

    private final MfaSecretService mfaSecretService = new MfaSecretService(
            new MfaSecretProperties("01234567890123456789012345678901"));

    @Test
    void shouldEncryptAndDecryptSecret() {
        String encrypted = mfaSecretService.encrypt("plain-mfa-secret");

        assertNotEquals("plain-mfa-secret", encrypted);
        assertTrue(encrypted.startsWith("ENC:"));
        assertEquals("plain-mfa-secret", mfaSecretService.decrypt(encrypted));
    }

    @Test
    void shouldReturnPlainSecretWhenStoredSecretIsNotEncrypted() {
        assertEquals("legacy-secret", mfaSecretService.decrypt("legacy-secret"));
    }
}
