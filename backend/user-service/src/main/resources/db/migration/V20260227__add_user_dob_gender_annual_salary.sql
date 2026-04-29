-- Add date_of_birth, gender, annual_salary columns to users (safe if already exists)
SET @dbname = DATABASE();

-- date_of_birth
SELECT @dob_exists := COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'date_of_birth';

SET @sql1 = IF(@dob_exists = 0,
  'ALTER TABLE users ADD COLUMN date_of_birth DATE NULL',
  'SELECT "users.date_of_birth already exists" AS message'
);

PREPARE stmt1 FROM @sql1;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

-- gender
SELECT @gender_exists := COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'gender';

SET @sql2 = IF(@gender_exists = 0,
  'ALTER TABLE users ADD COLUMN gender VARCHAR(16) NULL',
  'SELECT "users.gender already exists" AS message'
);

PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- annual_salary
SELECT @salary_exists := COUNT(*)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'annual_salary';

SET @sql3 = IF(@salary_exists = 0,
  'ALTER TABLE users ADD COLUMN annual_salary DOUBLE NULL',
  'SELECT "users.annual_salary already exists" AS message'
);

PREPARE stmt3 FROM @sql3;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;
