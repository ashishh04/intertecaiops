-- Ensure b2b_unit.type can store all EnumType.STRING values.
-- Some environments may still have this column as ENUM or a too-small VARCHAR,
-- causing "Data truncated" on insert when new B2BUnitType constants are added.
-- We normalize it to VARCHAR(64).

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
  'ALTER TABLE b2b_unit MODIFY COLUMN type VARCHAR(64) NOT NULL'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
