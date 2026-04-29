-- =====================================================================
-- V20260418__add_role_modules.sql
--
-- Adds the role_modules join table that tags each role with one or more
-- functional modules (HRMS, ECOMMERCE, PROJECT_MANAGEMENT). Lets the UI
-- show only roles that are valid in a given module when an admin is
-- assigning roles to a user (e.g. only HRMS roles appear in the HRMS
-- application).
--
-- Maps to the Role entity's @ElementCollection Set<RoleModule> modules.
-- =====================================================================

SET @dbname = DATABASE();

-- Detect UUID column type from roles.id to match existing schema
-- (BINARY(16) vs CHAR(36)). Keep compatible with existing migrations.
SELECT @roles_id_col_type := COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'roles'
  AND COLUMN_NAME = 'id';

SET @id_type = IF(LOWER(@roles_id_col_type) LIKE 'binary(%', 'BINARY(16)', 'CHAR(36)');

-- ---------------------------------------------------------------------
-- role_modules table
-- ---------------------------------------------------------------------
SELECT @role_modules_exists := COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'role_modules';

SET @sql = IF(@role_modules_exists = 0,
  CONCAT(
    'CREATE TABLE role_modules (',
    '  role_id ', @id_type, ' NOT NULL,',
    '  module VARCHAR(64) NOT NULL,',
    '  PRIMARY KEY (role_id, module),',
    '  KEY idx_role_modules_module (module),',
    '  CONSTRAINT fk_role_modules_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE',
    ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci'
  ),
  'SELECT "role_modules already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- Backfill sensible defaults for existing roles.
--
-- Strategy:
--   * ROLE_USER / ADMIN / ROLE_ADMIN are universal → tag with every module.
--   * BUSINESS_ADMIN / ROLE_BUSINESS_ADMIN belong to ECOMMERCE.
--   * PROJECT_MANAGER / TEAM_LEAD / DEVELOPER / QA / DESIGNER / ANALYST /
--     DEVOPS (and their ROLE_ prefixed variants) belong to PROJECT_MANAGEMENT.
--   * Names starting with HR_ or ROLE_HR_ belong to HRMS.
--   * Anything still untagged gets every module so nothing disappears
--     from existing admin screens until curated.
-- ---------------------------------------------------------------------

-- Universal roles → every module
INSERT IGNORE INTO role_modules (role_id, module)
SELECT r.id, m.module
FROM roles r
CROSS JOIN (
    SELECT 'HRMS'               AS module
    UNION ALL SELECT 'ECOMMERCE'
    UNION ALL SELECT 'PROJECT_MANAGEMENT'
) m
WHERE r.name IN ('ROLE_USER', 'ADMIN', 'ROLE_ADMIN');

-- Ecommerce / business admin roles
INSERT IGNORE INTO role_modules (role_id, module)
SELECT r.id, 'ECOMMERCE'
FROM roles r
WHERE r.name IN ('BUSINESS_ADMIN', 'ROLE_BUSINESS_ADMIN');

-- Project management roles
INSERT IGNORE INTO role_modules (role_id, module)
SELECT r.id, 'PROJECT_MANAGEMENT'
FROM roles r
WHERE r.name IN ('PROJECT_MANAGER', 'TEAM_LEAD', 'DEVELOPER',
                 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEAD', 'ROLE_DEVELOPER',
                 'QA', 'DESIGNER', 'ANALYST', 'DEVOPS');

-- HRMS roles (convention: name starts with HR_ or ROLE_HR_)
INSERT IGNORE INTO role_modules (role_id, module)
SELECT r.id, 'HRMS'
FROM roles r
WHERE r.name LIKE 'HR\_%' ESCAPE '\\'
   OR r.name LIKE 'ROLE\_HR\_%' ESCAPE '\\';

-- Catch-all: any role still without a module entry gets all three modules,
-- so it keeps showing up everywhere until explicitly curated.
INSERT IGNORE INTO role_modules (role_id, module)
SELECT r.id, m.module
FROM roles r
CROSS JOIN (
    SELECT 'HRMS'               AS module
    UNION ALL SELECT 'ECOMMERCE'
    UNION ALL SELECT 'PROJECT_MANAGEMENT'
) m
LEFT JOIN role_modules rm ON rm.role_id = r.id
WHERE rm.role_id IS NULL;
