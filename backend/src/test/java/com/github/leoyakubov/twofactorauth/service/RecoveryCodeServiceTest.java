package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecoveryCodeServiceTest {

    @Test
    void shouldGenerateAndConsumeRecoveryCodesWhenRecoveringAccess() {
        RecoveryCodeService service = new RecoveryCodeService(new BCryptPasswordEncoder());
        List<String> codes = service.generateRecoveryCodes();
        User user = new User();
        user.setUsername("demo");
        user.setRecoveryCodes(service.hashCodes(codes));

        assertEquals(8, codes.size());
        assertTrue(service.consumeRecoveryCode(user, codes.get(0)));
        assertFalse(service.consumeRecoveryCode(user, codes.get(0)));
    }
}
