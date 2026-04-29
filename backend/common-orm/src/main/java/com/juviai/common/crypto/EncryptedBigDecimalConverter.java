package com.juviai.common.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

/**
 * JPA {@link AttributeConverter} that transparently encrypts/decrypts {@link BigDecimal}
 * fields (e.g. salary, compensation amounts) annotated with
 * {@code @Convert(converter = EncryptedBigDecimalConverter.class)}.
 *
 * <h2>Storage format</h2>
 * The BigDecimal is serialized via {@link BigDecimal#toPlainString()} before encryption
 * so that scale and precision are preserved exactly (no scientific-notation rounding).
 * The resulting column must be {@code VARCHAR(512)} in the schema.
 *
 * <h2>Trade-off</h2>
 * Encrypting numeric fields disables SQL-level {@code ORDER BY}, {@code SUM}, {@code AVG}, etc.
 * All such operations must be performed in application code after decryption.
 * This is intentional: salary/compensation figures are high-sensitivity PII.
 *
 * <h2>Example</h2>
 * <pre>
 * {@literal @}Convert(converter = EncryptedBigDecimalConverter.class)
 * {@literal @}Column(name = "amount", length = 512)
 * private BigDecimal amount;
 * </pre>
 */
@Converter
public class EncryptedBigDecimalConverter implements AttributeConverter<BigDecimal, String> {

    @Override
    public String convertToDatabaseColumn(BigDecimal attribute) {
        if (attribute == null) {
            return null;
        }
        return AesFieldEncryptor.get().encrypt(attribute.toPlainString());
    }

    @Override
    public BigDecimal convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        String plaintext = AesFieldEncryptor.get().decrypt(dbData);
        if (plaintext == null || plaintext.isBlank()) {
            return null;
        }
        return new BigDecimal(plaintext);
    }
}
