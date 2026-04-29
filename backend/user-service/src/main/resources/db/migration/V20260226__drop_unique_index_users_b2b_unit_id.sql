-- Drop unique index/constraint on users.b2b_unit_id (may be created by previous @OneToOne mapping)
SET @dbname = DATABASE();

-- MySQL requires an index for foreign keys. If the FK is currently using a UNIQUE index,
-- create a NON-UNIQUE index first so we can safely drop the UNIQUE one.
SELECT @non_unique_exists := COUNT(*)
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'b2b_unit_id'
  AND NON_UNIQUE = 1;

SET @sql0 = IF(@non_unique_exists = 0,
  'CREATE INDEX idx_users_b2b_unit_id ON users (b2b_unit_id)',
  'SELECT "Non-unique index on users.b2b_unit_id already exists" AS message'
);

PREPARE stmt0 FROM @sql0;
EXECUTE stmt0;
DEALLOCATE PREPARE stmt0;

-- Prefer the known Hibernate-generated constraint/index name if present
SELECT @idx_exists := COUNT(*)
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'users'
  AND INDEX_NAME = 'UK_g9kls2tbwbskrovklh7d76mhn';

SET @sql = IF(@idx_exists > 0,
  'ALTER TABLE users DROP INDEX UK_g9kls2tbwbskrovklh7d76mhn',
  'SELECT "users.UK_g9kls2tbwbskrovklh7d76mhn not present" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- If the unique index has a different name, drop any UNIQUE index that includes only b2b_unit_id
SELECT @other_unique_idx := (
  SELECT s.INDEX_NAME
  FROM information_schema.STATISTICS s
  WHERE s.TABLE_SCHEMA = @dbname
    AND s.TABLE_NAME = 'users'
    AND s.COLUMN_NAME = 'b2b_unit_id'
    AND s.NON_UNIQUE = 0
    AND s.INDEX_NAME <> 'PRIMARY'
  LIMIT 1
);

SET @sql2 = IF(@other_unique_idx IS NOT NULL,
  CONCAT('ALTER TABLE users DROP INDEX ', @other_unique_idx),
  'SELECT "No other unique index on users.b2b_unit_id found" AS message'
);

PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;
