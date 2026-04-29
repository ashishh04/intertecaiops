-- Add store_id column to users (safe if already exists)
SET @dbname = DATABASE();

SELECT @users_id_col_type := COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'id';

SET @id_type = IF(LOWER(@users_id_col_type) LIKE 'binary(%', 'BINARY(16)', 'CHAR(36)');

SELECT @store_id_exists := COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'store_id';

SET @sql = IF(@store_id_exists = 0,
  CONCAT('ALTER TABLE users ADD COLUMN store_id ', @id_type, ' NULL'),
  'SELECT "users.store_id already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
