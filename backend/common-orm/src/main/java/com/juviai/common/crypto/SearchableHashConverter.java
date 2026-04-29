package com.juviai.common.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Produces a deterministic HMAC-SHA256 hash of a field for use in indexed lookup columns.
 *
 * <h2>Pattern: Encrypt + Hash (Blind Index)</h2>
 * <p>For fields like {@code email} or {@code mobile} that must be:
 * <ol>
 *   <li><b>Stored encrypted</b> (via {@link EncryptedStringConverter})</li>
 *   <li><b>Searchable</b> by exact match (e.g., login lookup)</li>
 * </ol>
 * Add a parallel {@code *Hash} column that stores the HMAC of the value:
 *
 * <pre>
 * {@literal @}Column(name = "email")
 * {@literal @}Convert(converter = EncryptedStringConverter.class)
 * private String email;
 *
 * {@literal @}Column(name = "email_hash", unique = true)
 * {@literal @}Convert(converter = SearchableHashConverter.class)
 * private String emailHash;   // set this whenever email is set
 * </pre>
 *
 * <p>Then query by hash:
 * <pre>
 * repository.findByEmailHash(SearchableHashConverter.hash(rawEmail));
 * </pre>
 *
 * <h2>Key</h2>
 * Uses the same {@code skillrat.field-encryption.key} as AES encryption.
 * A separate HMAC key is strongly recommended in production — set via
 * {@code skillrat.field-hash.key}.
 */
@Component
@Converter
public class SearchableHashConverter implements AttributeConverter<String, String> {

    private static SearchableHashConverter INSTANCE;

    @Value("${skillrat.field-hash.key:${skillrat.field-encryption.key:}}")
    private String base64HmacKey;

    @jakarta.annotation.PostConstruct
    void init() {
        INSTANCE = this;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return hash(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData; // hashes are NOT reversible; store separately from encrypted value
    }

    /**
     * Computes an HMAC-SHA256 blind index for the given plaintext value.
     * Returns null if the value is null/blank or the key is not configured.
     */
    public static String hash(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) return null;
        if (INSTANCE == null || INSTANCE.base64HmacKey == null || INSTANCE.base64HmacKey.isBlank()) {
            return plaintext; // no key configured: store plaintext (dev mode)
        }
        try {
            byte[] keyBytes = Base64.getDecoder().decode(INSTANCE.base64HmacKey.trim());
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
            byte[] hmac = mac.doFinal(plaintext.toLowerCase().trim().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            throw new IllegalStateException("Searchable hash computation failed", e);
        }
    }
}
