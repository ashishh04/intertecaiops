-- =============================================================================
-- Migration: Widen / retype columns for field-level encryption
-- Date: 2026-04-15
-- Services affected: user-service (users, compensations, educations,
--                    profile_experiences, experiences)
--
-- Why: EncryptedStringConverter / EncryptedBigDecimalConverter store values as
--      Base64(IV[12] || ciphertext || GCM-tag[16]).  For a plaintext of N chars
--      the stored width is ~ceil((N + 28) * 4/3) characters.  All encrypted
--      string columns are widened to VARCHAR(512) (handles ~350-char plaintexts);
--      free-text / long-description columns become TEXT.
--
--      Numeric columns (annual_salary DOUBLE, amount DECIMAL) are retyped to
--      VARCHAR(512) because the BigDecimal is serialised to its plain-string
--      form before encryption.
--
-- IMPORTANT – existing plaintext data
--   After this DDL runs, existing rows still contain plaintext.  A one-off
--   data-migration job must re-encrypt them before the service goes live with
--   encryption enabled (SKILLRAT_FIELD_ENC_KEY set).  Until then, the
--   AesFieldEncryptor gracefully returns plaintext as-is when no key is
--   configured (dev / staging without the key set will still work).
-- =============================================================================

-- ─── users (SINGLE_TABLE for Employee.annualSalary) ──────────────────────────
-- annual_salary was DOUBLE NULL; now stores encrypted BigDecimal as VARCHAR
ALTER TABLE users
    MODIFY COLUMN annual_salary VARCHAR(512) NULL;

-- ─── compensations ────────────────────────────────────────────────────────────
-- amount was DECIMAL(15,2) NOT NULL; now stores encrypted BigDecimal as VARCHAR
ALTER TABLE compensations
    MODIFY COLUMN amount VARCHAR(512) NOT NULL;

-- ─── educations ──────────────────────────────────────────────────────────────
-- institution VARCHAR(200), degree VARCHAR(120), field_of_study VARCHAR(120)
-- → all widened to VARCHAR(512) for AES-GCM ciphertext
ALTER TABLE educations
    MODIFY COLUMN institution   VARCHAR(512) NOT NULL,
    MODIFY COLUMN degree        VARCHAR(512) NOT NULL,
    MODIFY COLUMN field_of_study VARCHAR(512) NULL;

-- ─── profile_experiences ─────────────────────────────────────────────────────
-- title VARCHAR(200), organization_name VARCHAR(200) → VARCHAR(512)
-- description VARCHAR(2000) → TEXT (free-text; encrypted ciphertext can be long)
ALTER TABLE profile_experiences
    MODIFY COLUMN title             VARCHAR(512) NOT NULL,
    MODIFY COLUMN organization_name VARCHAR(512) NULL,
    MODIFY COLUMN description       TEXT         NULL;

-- ─── experiences ─────────────────────────────────────────────────────────────
-- role was VARCHAR(255) (Hibernate default); widen to VARCHAR(512)
ALTER TABLE experiences
    MODIFY COLUMN role VARCHAR(512) NULL;
