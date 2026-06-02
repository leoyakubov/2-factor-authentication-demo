package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Slf4j
public class RecoveryCodeManager {

    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int GROUP_SIZE = 4;
    private static final int GROUP_COUNT = 2;

    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public RecoveryCodeManager(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public List<String> generateRecoveryCodes(int count) {
        List<String> codes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            codes.add(generateCode());
        }
        return codes;
    }

    public Set<String> hashCodes(List<String> plainCodes) {
        Set<String> hashes = new HashSet<>();
        for (String code : plainCodes) {
            hashes.add(passwordEncoder.encode(normalize(code)));
        }
        return hashes;
    }

    public boolean consumeRecoveryCode(User user, String code) {
        if (user.getRecoveryCodes() == null || user.getRecoveryCodes().isEmpty()) {
            return false;
        }

        String normalized = normalize(code);
        for (String hashedCode : new HashSet<>(user.getRecoveryCodes())) {
            if (passwordEncoder.matches(normalized, hashedCode)) {
                user.getRecoveryCodes().remove(hashedCode);
                log.info("consumed recovery code for {}", user.getUsername());
                return true;
            }
        }

        return false;
    }

    private String generateCode() {
        StringBuilder builder = new StringBuilder();
        for (int group = 0; group < GROUP_COUNT; group++) {
            if (group > 0) {
                builder.append('-');
            }

            for (int index = 0; index < GROUP_SIZE; index++) {
                builder.append(ALPHABET[secureRandom.nextInt(ALPHABET.length)]);
            }
        }

        return builder.toString().toUpperCase(Locale.ROOT);
    }

    private String normalize(String code) {
        return code == null ? "" : code.replace("-", "").replace(" ", "").trim().toUpperCase(Locale.ROOT);
    }
}
