package com.juviai.user.crypto;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KeyVaultEnvelopeEncryptionService {

    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;

    private final SecureRandom secureRandom = new SecureRandom();

    private final String keyVaultUrl;
    private final String keyName;
    private final String keyVersion;

    private final TokenCredential credential;

    private volatile String cachedKeyId;

    public KeyVaultEnvelopeEncryptionService(
            @Value("${skillrat.crypto.keyvault.url:}") String keyVaultUrl,
            @Value("${skillrat.crypto.keyvault.key-name:}") String keyName,
            @Value("${skillrat.crypto.keyvault.key-version:}") String keyVersion) {
        this.keyVaultUrl = keyVaultUrl;
        this.keyName = keyName;
        this.keyVersion = keyVersion;
        this.credential = new DefaultAzureCredentialBuilder().build();
    }

    public String encryptToPayload(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] dekBytes = new byte[32];
            secureRandom.nextBytes(dekBytes);
            SecretKey dek = new SecretKeySpec(dekBytes, "AES");

            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, dek, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            WrapResult wrapped = cryptographyClient().wrapKey(KeyWrapAlgorithm.RSA_OAEP_256, dekBytes);

            String kid = keyId();
            String payload = "{\"v\":1," +
                    "\"kid\":\"" + escapeJson(kid) + "\"," +
                    "\"alg\":\"AES/GCM\"," +
                    "\"wrap\":\"RSA_OAEP_256\"," +
                    "\"iv\":\"" + b64(iv) + "\"," +
                    "\"ct\":\"" + b64(ciphertext) + "\"," +
                    "\"dek\":\"" + b64(wrapped.getEncryptedKey()) + "\"}";
            return payload;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt using Key Vault envelope encryption", e);
        }
    }

    public String decryptFromPayload(String payload) {
        if (payload == null) return null;
        try {
            if (!payload.startsWith("{\"v\":")) {
                // Backward compatibility: old rows encrypted with JUVIAI_AES_KEY AttributeConverter
                return new AesStringAttributeConverter().convertToEntityAttribute(payload);
            }

            String kid = jsonGet(payload, "kid");
            String ivB64 = jsonGet(payload, "iv");
            String ctB64 = jsonGet(payload, "ct");
            String dekB64 = jsonGet(payload, "dek");

            byte[] iv = Base64.getDecoder().decode(ivB64);
            byte[] ciphertext = Base64.getDecoder().decode(ctB64);
            byte[] wrappedDek = Base64.getDecoder().decode(dekB64);

            CryptographyClient crypto = cryptographyClient(kid);
            UnwrapResult unwrapped = crypto.unwrapKey(KeyWrapAlgorithm.RSA_OAEP_256, wrappedDek);
            byte[] dekBytes = unwrapped.getKey();

            SecretKey dek = new SecretKeySpec(dekBytes, "AES");
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, dek, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt using Key Vault envelope encryption", e);
        }
    }

    public void validateConfiguration() {
        // Will throw if config/credential/key is invalid
        String test = "keyvault-envelope-test";
        String enc = encryptToPayload(test);
        String dec = decryptFromPayload(enc);
        if (!test.equals(dec)) {
            throw new IllegalStateException("Key Vault envelope encryption validation failed");
        }
    }

    private CryptographyClient cryptographyClient() {
        return cryptographyClient(keyId());
    }

    private CryptographyClient cryptographyClient(String keyId) {
        return new CryptographyClientBuilder()
                .keyIdentifier(keyId)
                .credential(credential)
                .buildClient();
    }

    private String keyId() {
        String existing = cachedKeyId;
        if (existing != null && !existing.isBlank()) {
            return existing;
        }
        if (keyVaultUrl == null || keyVaultUrl.isBlank()) {
            throw new IllegalStateException("Missing Key Vault configuration: skillrat.crypto.keyvault.url. "
                    + "Injected values: url='" + keyVaultUrl + "', key-name='" + keyName + "', key-version='" + keyVersion + "'. "
                    + "Active profile hints: spring.profiles.active(sysprop)='" + System.getProperty("spring.profiles.active") + "', "
                    + "SPRING_PROFILES_ACTIVE(env)='" + System.getenv("SPRING_PROFILES_ACTIVE") + "'.");
        }
        if (keyName == null || keyName.isBlank()) {
            throw new IllegalStateException("Missing Key Vault configuration: skillrat.crypto.keyvault.key-name. "
                    + "Injected values: url='" + keyVaultUrl + "', key-name='" + keyName + "', key-version='" + keyVersion + "'. "
                    + "Active profile hints: spring.profiles.active(sysprop)='" + System.getProperty("spring.profiles.active") + "', "
                    + "SPRING_PROFILES_ACTIVE(env)='" + System.getenv("SPRING_PROFILES_ACTIVE") + "'.");
        }

        KeyClient keyClient = new KeyClientBuilder()
                .vaultUrl(keyVaultUrl)
                .credential(credential)
                .buildClient();

        String id = (keyVersion == null || keyVersion.isBlank())
                ? keyClient.getKey(keyName).getId()
                : keyClient.getKey(keyName, keyVersion).getId();

        cachedKeyId = id;
        return id;
    }

    private static String b64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String jsonGet(String json, String key) {
        // Minimal JSON field extractor for the exact payload format we create.
        String needle = "\"" + key + "\":\"";
        int start = json.indexOf(needle);
        if (start < 0) {
            throw new IllegalArgumentException("Invalid encrypted payload: missing '" + key + "'");
        }
        start += needle.length();
        int end = json.indexOf('"', start);
        if (end < 0) {
            throw new IllegalArgumentException("Invalid encrypted payload: unterminated '" + key + "'");
        }
        return json.substring(start, end);
    }
}
