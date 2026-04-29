package com.juviai.common.crypto;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM field-level encryption utility.
 *
 * <p>Used by JPA {@link EncryptedStringConverter} to transparently encrypt/decrypt
 * sensitive columns at the application layer before they are stored in the database.
 *
 * <h2>Key management</h2>
 * <ul>
 *   <li>Set {@code JUVIAI_FIELD_ENC_KEY} environment variable to a Base64-encoded 32-byte key.</li>
 *   <li>Generate a key with: {@code openssl rand -base64 32}</li>
 *   <li>Never commit the key to source control.</li>
 *   <li>Rotate keys by decrypting old data → re-encrypting with new key (offline migration).</li>
 * </ul>
 *
 * <h2>Format stored in DB</h2>
 * {@code BASE64(IV[12 bytes] || CIPHERTEXT || GCM_TAG[16 bytes])}
 */
@Component
public class AesFieldEncryptor {

    private static final Logger log = LoggerFactory.getLogger(AesFieldEncryptor.class);

    private static final String ALGORITHM     = "AES/GCM/NoPadding";
    private static final int    IV_LENGTH     = 12;   // 96-bit IV recommended for GCM
    private static final int    TAG_LENGTH    = 128;  // 128-bit authentication tag
    private static final String KEY_ALGORITHM = "AES";

    /** Singleton instance — set by Spring after construction. */
    private static AesFieldEncryptor INSTANCE;

    @Value("${skillrat.field-encryption.key:}")
    private String base64Key;

    private SecretKey secretKey;
    private final SecureRandom random = new SecureRandom();

    @PostConstruct
    void init() {
        if (base64Key == null || base64Key.isBlank()) {
            log.warn("JUVIAI_FIELD_ENC_KEY / skillrat.field-encryption.key is not set. " +
                     "Field-level encryption is DISABLED. Set this key in production.");
            secretKey = null;
        } else {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key.trim());
            if (keyBytes.length != 32) {
                throw new IllegalStateException(
                        "Field encryption key must be exactly 32 bytes (256 bits). " +
                        "Generate with: openssl rand -base64 32");
            }
            secretKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
            log.info("Field-level AES-256-GCM encryption enabled.");
        }
        INSTANCE = this;
    }

    public static AesFieldEncryptor get() {
        if (INSTANCE == null) {
            throw new IllegalStateException(
                    "AesFieldEncryptor not yet initialized by Spring. " +
                    "Ensure common-orm is in the component scan.");
        }
        return INSTANCE;
    }

    /**
     * Encrypts a plaintext string.
     *
     * @param plaintext the value to encrypt; null/blank is returned as-is
     * @return Base64-encoded {@code IV || ciphertext || tag}, or the original value if encryption is disabled
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isBlank() || secretKey == null) {
            return plaintext;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Prepend IV to ciphertext
            byte[] payload = new byte[IV_LENGTH + ciphertext.length];
            System.arraycopy(iv, 0, payload, 0, IV_LENGTH);
            System.arraycopy(ciphertext, 0, payload, IV_LENGTH, ciphertext.length);

            return Base64.getEncoder().encodeToString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Field encryption failed", e);
        }
    }

    /**
     * Decrypts a previously encrypted value.
     *
     * @param ciphertext the Base64-encoded payload; null/blank is returned as-is
     * @return the original plaintext, or the original value if encryption is disabled
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isBlank() || secretKey == null) {
            return ciphertext;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(ciphertext);
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(payload, 0, iv, 0, IV_LENGTH);
            byte[] actualCiphertext = new byte[payload.length - IV_LENGTH];
            System.arraycopy(payload, IV_LENGTH, actualCiphertext, 0, actualCiphertext.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] plainBytes = cipher.doFinal(actualCiphertext);

            return new String(plainBytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Field decryption failed — wrong key or corrupted data", e);
        }
    }
}
