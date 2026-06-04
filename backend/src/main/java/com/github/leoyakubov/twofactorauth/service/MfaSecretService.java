package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.config.properties.MfaSecretProperties;
import com.github.leoyakubov.twofactorauth.exception.InternalServerException;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class MfaSecretService {

    private static final String ENCRYPTED_PREFIX = "ENC:";
    private static final String AES_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKeySpec keySpec;

    public MfaSecretService(MfaSecretProperties properties) {
        this.keySpec = new SecretKeySpec(deriveKey(properties.encryptionKey()), AES_ALGORITHM);
    }

    public String encrypt(String secret) {
        if (secret == null || secret.isBlank()) {
            return secret;
        }

        byte[] iv = new byte[IV_BYTES];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));
            return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(ByteBuffer
                    .allocate(iv.length + encrypted.length)
                    .put(iv)
                    .put(encrypted)
                    .array());
        } catch (GeneralSecurityException ex) {
            throw new InternalServerException("unable to protect MFA secret");
        }
    }

    public String decrypt(String storedSecret) {
        if (storedSecret == null || storedSecret.isBlank() || !storedSecret.startsWith(ENCRYPTED_PREFIX)) {
            return storedSecret;
        }

        try {
            byte[] payload = Base64.getDecoder().decode(storedSecret.substring(ENCRYPTED_PREFIX.length()));
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[IV_BYTES];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException ex) {
            throw new InternalServerException("unable to read MFA secret");
        }
    }

    private byte[] deriveKey(String encryptionKey) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new InternalServerException("unable to initialize MFA secret encryption");
        }
    }
}
