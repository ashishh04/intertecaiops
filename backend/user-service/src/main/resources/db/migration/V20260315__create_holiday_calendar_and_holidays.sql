-- Create holiday_calendar and holidays tables (safe if already exists)
SET @dbname = DATABASE();

-- Detect UUID column type from users.id to match existing schema (BINARY(16) vs CHAR(36))
SELECT @users_id_col_type := COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'id';

SET @id_type = IF(LOWER(@users_id_col_type) LIKE 'binary(%', 'BINARY(16)', 'CHAR(36)');

-- holiday_calendar
SELECT @holiday_calendar_exists := COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'holiday_calendar';

SET @sql = IF(@holiday_calendar_exists = 0,
  CONCAT(
    'CREATE TABLE holiday_calendar (',
    '  id ', @id_type, ' NOT NULL,',
    '  created_date TIMESTAMP NULL,',
    '  updated_date TIMESTAMP NULL,',
    '  created_by VARCHAR(255) NULL,',
    '  updated_by VARCHAR(255) NULL,',
    '  tenant_id VARCHAR(255) NULL,',
    '  name VARCHAR(200) NOT NULL,',
    '  b2b_unit_id ', @id_type, ' NOT NULL,',
    '  city_id ', @id_type, ' NOT NULL,',
    '  PRIMARY KEY (id),',
    '  UNIQUE KEY uk_holiday_calendar_city_tenant (city_id, tenant_id),',
    '  KEY idx_holiday_calendar_b2b_tenant (b2b_unit_id, tenant_id),',
    '  KEY idx_holiday_calendar_city_tenant (city_id, tenant_id),',
    '  CONSTRAINT fk_holiday_calendar_b2b_unit FOREIGN KEY (b2b_unit_id) REFERENCES b2b_unit(id),',
    '  CONSTRAINT fk_holiday_calendar_city FOREIGN KEY (city_id) REFERENCES cities(id)',
    ') ENGINE=InnoDB'
  ),
  'SELECT "holiday_calendar already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- holidays
SELECT @holidays_exists := COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'holidays';

SET @sql = IF(@holidays_exists = 0,
  CONCAT(
    'CREATE TABLE holidays (',
    '  id ', @id_type, ' NOT NULL,',
    '  created_date TIMESTAMP NULL,',
    '  updated_date TIMESTAMP NULL,',
    '  created_by VARCHAR(255) NULL,',
    '  updated_by VARCHAR(255) NULL,',
    '  tenant_id VARCHAR(255) NULL,',
    '  holiday_calendar_id ', @id_type, ' NOT NULL,',
    '  holiday_date DATE NOT NULL,',
    '  name VARCHAR(200) NOT NULL,',
    '  PRIMARY KEY (id),',
    '  UNIQUE KEY uk_holiday_calendar_date_tenant (holiday_calendar_id, holiday_date, tenant_id),',
    '  KEY idx_holiday_calendar_tenant (holiday_calendar_id, tenant_id),',
    '  KEY idx_holiday_date_tenant (holiday_date, tenant_id),',
    '  CONSTRAINT fk_holidays_holiday_calendar FOREIGN KEY (holiday_calendar_id) REFERENCES holiday_calendar(id)',
    ') ENGINE=InnoDB'
  ),
  'SELECT "holidays already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
