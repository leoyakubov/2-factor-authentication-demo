package com.github.leoyakubov.twofactorauth.service;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpServiceTest {

    private final TotpService totpService = new TotpService();

    @Test
    void shouldReturnValueWhenGeneratingSecret() {
        assertNotNull(totpService.generateSecret());
    }

    @Test
    void shouldReturnDataUriWhenBuildingQrCode() {
        String uri = totpService.getUriForImage("JBSWY3DPEHPK3PXP");

        assertTrue(uri.startsWith("data:image/png;base64,"));
    }

    @Test
    void shouldRejectBlankSecretWhenBuildingQrCode() {
        assertThrows(IllegalArgumentException.class, () -> totpService.getUriForImage(" "));
    }

    @Test
    void shouldAcceptCurrentCodeAndRejectWrongCodeWhenVerifyingTotp() throws Exception {
        String secret = totpService.generateSecret();
        DefaultCodeGenerator codeGenerator = new DefaultCodeGenerator();
        long currentWindow = Math.floorDiv(new SystemTimeProvider().getTime(), 30);
        String code = codeGenerator.generate(secret, currentWindow);

        assertTrue(totpService.verifyCode(code, secret));
        assertFalse(totpService.verifyCode("000000", secret));
    }
}
