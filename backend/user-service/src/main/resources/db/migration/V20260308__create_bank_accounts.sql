-- Create bank_accounts table with 1:1 mapping to users (safe if already exists)
SET @dbname = DATABASE();

SELECT @table_exists := COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'bank_accounts';

SELECT @users_id_col_type := COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'id';

SET @id_type = IF(LOWER(@users_id_col_type) LIKE 'binary(%', 'BINARY(16)', 'CHAR(36)');

SET @sql = IF(@table_exists = 0,
  CONCAT(
    'CREATE TABLE bank_accounts (',
    '  id ', @id_type, ' NOT NULL,',
    '  created_date TIMESTAMP NULL,',
    '  updated_date TIMESTAMP NULL,',
    '  created_by VARCHAR(255) NULL,',
    '  updated_by VARCHAR(255) NULL,',
    '  tenant_id VARCHAR(255) NULL,',
    '  user_id ', @id_type, ' NOT NULL,',
    '  account_number VARCHAR(512) NOT NULL,',
    '  ifsc_code VARCHAR(512) NOT NULL,',
    '  PRIMARY KEY (id),',
    '  UNIQUE KEY uk_bank_accounts_user_id (user_id),',
    '  CONSTRAINT fk_bank_accounts_user FOREIGN KEY (user_id) REFERENCES users(id)',
    ') ENGINE=InnoDB'
  ),
  'SELECT "bank_accounts already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
