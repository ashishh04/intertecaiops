-- Widen columns on Employee, BankAccount, Address, B2BUnit for AES-GCM ciphertext
-- AES-GCM output (base64): ~2x plaintext length + 12 bytes IV overhead

SET @alter_employees_pf_number := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE employees MODIFY COLUMN pf_number VARCHAR(512)'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'employees'
      AND column_name = 'pf_number'
);
PREPARE stmt FROM @alter_employees_pf_number;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @alter_employees_uan_number := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE employees MODIFY COLUMN uan_number VARCHAR(512)'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'employees'
      AND column_name = 'uan_number'
);
PREPARE stmt FROM @alter_employees_uan_number;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @alter_employees_pan_number := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE employees MODIFY COLUMN pan_number VARCHAR(512)'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'employees'
      AND column_name = 'pan_number'
);
PREPARE stmt FROM @alter_employees_pan_number;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @alter_bank_accounts_account_number := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE bank_accounts MODIFY COLUMN account_number TEXT'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'bank_accounts'
      AND column_name = 'account_number'
);
PREPARE stmt FROM @alter_bank_accounts_account_number;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @alter_bank_accounts_ifsc_code := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE bank_accounts MODIFY COLUMN ifsc_code TEXT'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'bank_accounts'
      AND column_name = 'ifsc_code'
);
PREPARE stmt FROM @alter_bank_accounts_ifsc_code;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @alter_bank_accounts_account_holder_name := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE bank_accounts MODIFY COLUMN account_holder_name TEXT'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'bank_accounts'
      AND column_name = 'account_holder_name'
);
PREPARE stmt FROM @alter_bank_accounts_account_holder_name;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @alter_addresses_name := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE addresses MODIFY COLUMN name VARCHAR(512)'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'addresses'
      AND column_name = 'name'
);
PREPARE stmt FROM @alter_addresses_name;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @alter_addresses_mobile_number := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE addresses MODIFY COLUMN mobile_number VARCHAR(512)'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'addresses'
      AND column_name = 'mobile_number'
);
PREPARE stmt FROM @alter_addresses_mobile_number;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @alter_addresses_line1 := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE addresses MODIFY COLUMN line1 VARCHAR(512)'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'addresses'
      AND column_name = 'line1'
);
PREPARE stmt FROM @alter_addresses_line1;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @alter_addresses_line2 := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE addresses MODIFY COLUMN line2 VARCHAR(512)'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'addresses'
      AND column_name = 'line2'
);
PREPARE stmt FROM @alter_addresses_line2;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @alter_addresses_postal_code := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE addresses MODIFY COLUMN postal_code VARCHAR(64)'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'addresses'
      AND column_name = 'postal_code'
);
PREPARE stmt FROM @alter_addresses_postal_code;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @alter_addresses_full_text := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE addresses MODIFY COLUMN full_text TEXT'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'addresses'
      AND column_name = 'full_text'
);
PREPARE stmt FROM @alter_addresses_full_text;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @alter_b2b_units_contact_email := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE b2b_units MODIFY COLUMN contact_email VARCHAR(512)'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'b2b_units'
      AND column_name = 'contact_email'
);
PREPARE stmt FROM @alter_b2b_units_contact_email;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @alter_b2b_units_contact_phone := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE b2b_units MODIFY COLUMN contact_phone VARCHAR(512)'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'b2b_units'
      AND column_name = 'contact_phone'
);
PREPARE stmt FROM @alter_b2b_units_contact_phone;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @alter_b2b_units_pan_number := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE b2b_units MODIFY COLUMN pan_number VARCHAR(512)'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'b2b_units'
      AND column_name = 'pan_number'
);
PREPARE stmt FROM @alter_b2b_units_pan_number;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @alter_b2b_units_cin_number := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE b2b_units MODIFY COLUMN cin_number VARCHAR(512)'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'b2b_units'
      AND column_name = 'cin_number'
);
PREPARE stmt FROM @alter_b2b_units_cin_number;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @alter_b2b_units_gst_number := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE b2b_units MODIFY COLUMN gst_number VARCHAR(512)'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'b2b_units'
      AND column_name = 'gst_number'
);
PREPARE stmt FROM @alter_b2b_units_gst_number;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @alter_b2b_units_tan_number := (
    SELECT IF(
        COUNT(*) = 0,
        'SELECT 1',
        'ALTER TABLE b2b_units MODIFY COLUMN tan_number VARCHAR(512)'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'b2b_units'
      AND column_name = 'tan_number'
);
PREPARE stmt FROM @alter_b2b_units_tan_number;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
