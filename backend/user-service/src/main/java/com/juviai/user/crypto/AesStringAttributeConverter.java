package com.juviai.user.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
public class AesStringAttributeConverter implements AttributeConverter<String, String> {

    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;

    private static final SecureRandom secureRandom = new SecureRandom();

    public static void validateConfiguredKey() {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid encryption configuration for JUVIAI_AES_KEY", e);
        }
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            byte[] out = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ciphertext, 0, out, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt attribute", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            byte[] all = Base64.getDecoder().decode(dbData);
            if (all.length < IV_LENGTH_BYTES + 1) {
                throw new IllegalArgumentException("Invalid encrypted payload");
            }

            byte[] iv = new byte[IV_LENGTH_BYTES];
            byte[] ciphertext = new byte[all.length - IV_LENGTH_BYTES];
            System.arraycopy(all, 0, iv, 0, IV_LENGTH_BYTES);
            System.arraycopy(all, IV_LENGTH_BYTES, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt attribute", e);
        }
    }

    private static SecretKey secretKey() {
        String raw = System.getProperty("JUVIAI_AES_KEY");
        if (raw == null || raw.isBlank()) {
            raw = System.getenv("JUVIAI_AES_KEY");
        }
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("Missing encryption key. Set JUVIAI_AES_KEY as env var or JVM property (base64/base64url encoded AES key; 16/24/32 bytes).",
                    null);
        }

        String normalized = normalizeKeyString(raw);
        byte[] key = decodeBase64Lenient(normalized);
        if (!(key.length == 16 || key.length == 24 || key.length == 32)) {
            throw new IllegalStateException("Invalid JUVIAI_AES_KEY length. Expected 16/24/32 bytes after base64 decoding, got " + key.length + ".");
        }
        return new SecretKeySpec(key, "AES");
    }

    private static String normalizeKeyString(String raw) {
        String s = raw.trim();
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1).trim();
        }
        return s;
    }

    private static byte[] decodeBase64Lenient(String key) {
        try {
            return Base64.getDecoder().decode(key);
        } catch (IllegalArgumentException ignore) {
            try {
                return Base64.getUrlDecoder().decode(key);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid JUVIAI_AES_KEY. Value must be base64/base64url encoded.", e);
            }
        }
    }
}
