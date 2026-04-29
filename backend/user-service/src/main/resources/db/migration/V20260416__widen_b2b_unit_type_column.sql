-- Ensure b2b_unit.type can store all EnumType.STRING values (e.g., FOOD_DELIVERY)
-- Some environments may have created this column as ENUM with a limited set, causing
-- "Data truncated" on insert. We normalize it to VARCHAR(32).

SET @dbname = DATABASE();

SELECT @type_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @dbname
    AND TABLE_NAME = 'b2b_unit'
    AND COLUMN_NAME = 'type'
);

SET @sql := IF(
  @type_exists = 0,
  'SELECT "b2b_unit.type column not found; skipping" AS message',
  'ALTER TABLE b2b_unit MODIFY COLUMN type VARCHAR(32) NOT NULL'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
