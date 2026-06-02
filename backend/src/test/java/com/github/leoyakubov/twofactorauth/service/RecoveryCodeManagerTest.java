package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecoveryCodeManagerTest {

    @Test
    void shouldGenerateAndConsumeRecoveryCodes() {
        RecoveryCodeManager manager = new RecoveryCodeManager(new BCryptPasswordEncoder());
        List<String> codes = manager.generateRecoveryCodes(2);
        User user = new User();
        user.setUsername("demo");
        user.setRecoveryCodes(manager.hashCodes(codes));

        assertEquals(2, codes.size());
        assertTrue(manager.consumeRecoveryCode(user, codes.get(0)));
        assertFalse(manager.consumeRecoveryCode(user, codes.get(0)));
    }
}
