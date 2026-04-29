-- Expand bank_accounts columns to hold Key Vault envelope encryption payload
SET @dbname = DATABASE();

SELECT @table_exists := COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'bank_accounts';

SET @sql = IF(@table_exists = 1,
  'ALTER TABLE bank_accounts MODIFY COLUMN account_number TEXT NOT NULL, MODIFY COLUMN ifsc_code TEXT NOT NULL',
  'SELECT "bank_accounts does not exist" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
