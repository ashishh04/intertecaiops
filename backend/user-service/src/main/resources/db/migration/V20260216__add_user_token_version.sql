-- Add token_version to users for JWT tokenVersion strategy (safe if already exists)
SET @dbname = DATABASE();

SELECT @col_exists := COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'token_version';

SET @sql = IF(@col_exists = 0,
  'ALTER TABLE users ADD COLUMN token_version INT NOT NULL DEFAULT 0',
  'SELECT "users.token_version already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
