-- Add amount column to salary_components table
SET @dbname = DATABASE();

SELECT @table_exists := COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'salary_components';

SELECT @col_exists := COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'salary_components'
  AND COLUMN_NAME = 'amount';

SET @sql = IF(@table_exists > 0 AND @col_exists = 0,
  'ALTER TABLE salary_components ADD COLUMN amount DECIMAL(15,2)',
  'SELECT "salary_components missing or amount already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
