-- Create compensations table (history) with many:1 mapping to employees/users (safe if already exists)
SET @dbname = DATABASE();

SELECT @table_exists := COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'compensations';

SELECT @users_id_col_type := COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'id';

SET @id_type = IF(LOWER(@users_id_col_type) LIKE 'binary(%', 'BINARY(16)', 'CHAR(36)');

SET @sql = IF(@table_exists = 0,
  CONCAT(
    'CREATE TABLE compensations (',
    '  id ', @id_type, ' NOT NULL,',
    '  created_date TIMESTAMP NULL,',
    '  updated_date TIMESTAMP NULL,',
    '  created_by VARCHAR(255) NULL,',
    '  updated_by VARCHAR(255) NULL,',
    '  tenant_id VARCHAR(255) NULL,',
    '  employee_id ', @id_type, ' NOT NULL,',
    '  type VARCHAR(32) NOT NULL,',
    '  amount DECIMAL(15,2) NOT NULL,',
    '  effective_start_date DATE NOT NULL,',
    '  effective_end_date DATE NULL,',
    '  active BOOLEAN NOT NULL DEFAULT 1,',
    '  PRIMARY KEY (id),',
    '  KEY idx_comp_employee_id (employee_id),',
    '  KEY idx_comp_employee_active (employee_id, active),',
    '  KEY idx_comp_employee_type (employee_id, type),',
    '  CONSTRAINT fk_compensations_employee FOREIGN KEY (employee_id) REFERENCES users(id)',
    ') ENGINE=InnoDB'
  ),
  'SELECT "compensations already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
