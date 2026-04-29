-- =============================================================================
-- Migration: Encrypt PII fields on users table
-- Date: 2026-04-14
-- Purpose: Add blind-index hash columns for email and mobile (required because
--          the actual columns will store AES-256-GCM ciphertext and cannot be
--          searched via SQL LIKE / equality without the hash columns).
--
-- After this migration runs, the application code (AesFieldEncryptor +
-- EncryptedStringConverter) will encrypt new writes automatically.
--
-- IMPORTANT: Existing plaintext data must be re-encrypted via the
--            data migration job (UserPiiEncryptionMigrationJob) before
--            going live. Run that job in a maintenance window.
-- =============================================================================

-- Step 1: Add hash columns (nullable initially — filled by migration job)
SET @add_email_hash := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE users ADD COLUMN email_hash VARCHAR(64) NULL',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'email_hash'
);
PREPARE stmt FROM @add_email_hash;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mobile_hash := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE users ADD COLUMN mobile_hash VARCHAR(64) NULL',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'mobile_hash'
);
PREPARE stmt FROM @add_mobile_hash;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 2: Widen email and mobile columns to hold AES-GCM ciphertext
--         (IV[12] + ciphertext + tag[16], Base64 encoded ~2x plaintext length)
ALTER TABLE users
    MODIFY COLUMN email      VARCHAR(512) NOT NULL,
    MODIFY COLUMN mobile     VARCHAR(512) NULL,
    MODIFY COLUMN first_name VARCHAR(512) NOT NULL,
    MODIFY COLUMN last_name  VARCHAR(512) NOT NULL;

-- Step 3: Unique indexes on hash columns (replaces unique constraint on raw email)
-- Remove old unique constraints first if they exist
SET @drop_users_email_key := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'DROP INDEX users_email_key ON users'
    )
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND index_name = 'users_email_key'
);
PREPARE stmt FROM @drop_users_email_key;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_uk_users_email := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'DROP INDEX uk_users_email ON users'
    )
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND index_name = 'uk_users_email'
);
PREPARE stmt FROM @drop_uk_users_email;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @create_uk_users_email_hash := (
    SELECT IF(
        COUNT(*) = 0,
        'CREATE UNIQUE INDEX uk_users_email_hash ON users (email_hash)',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND index_name = 'uk_users_email_hash'
);
PREPARE stmt FROM @create_uk_users_email_hash;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @create_uk_users_mobile_hash := (
    SELECT IF(
        COUNT(*) = 0,
        'CREATE UNIQUE INDEX uk_users_mobile_hash ON users (mobile_hash)',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND index_name = 'uk_users_mobile_hash'
);
PREPARE stmt FROM @create_uk_users_mobile_hash;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
