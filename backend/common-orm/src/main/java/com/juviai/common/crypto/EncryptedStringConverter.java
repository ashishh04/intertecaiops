package com.juviai.common.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA {@link AttributeConverter} that transparently encrypts/decrypts {@code String}
 * fields annotated with {@code @Convert(converter = EncryptedStringConverter.class)}.
 *
 * <h2>Usage</h2>
 * <pre>
 * {@literal @}Convert(converter = EncryptedStringConverter.class)
 * private String mobile;
 * </pre>
 *
 * <h2>Searching encrypted fields</h2>
 * <p>AES-GCM uses a random IV per encryption, so two encryptions of the same value produce
 * different ciphertexts. This means you CANNOT use SQL {@code WHERE mobile = ?} directly.
 * For fields that need to be searched (e.g., email login lookup), use a separate
 * deterministic hash column (see {@link SearchableHashConverter}) for lookups while
 * keeping the actual value encrypted.
 *
 * <h2>Key not set</h2>
 * <p>If {@code JUVIAI_FIELD_ENC_KEY} is not configured, values are stored as plaintext
 * with a warning logged. This allows local development without mandatory key setup.
 */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return AesFieldEncryptor.get().encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return AesFieldEncryptor.get().decrypt(dbData);
    }
}
