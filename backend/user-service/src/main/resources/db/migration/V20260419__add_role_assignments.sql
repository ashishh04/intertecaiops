-- =====================================================================
-- V20260419__add_role_assignments.sql
--
-- Introduces the generic scoped-role-assignment model. Replaces (without
-- yet dropping) the single-purpose junction tables business_user_roles
-- and project_user_roles with one polymorphic role_assignments table.
--
-- Tables created:
--   role_scopes       — applicable ScopeType values per Role
--   role_assignments  — (user, role, scopeType, scopeId) triples
--
-- Backfill:
--   role_scopes      — seed applicable scopes for well-known role names
--   role_assignments — copy rows from business_user_roles   (scope=B2B_UNIT)
--                      and from project_user_roles         (scope=PROJECT)
--
-- The legacy tables are left intact for one release while BusinessRoleService
-- and ProjectRoleService are reimplemented to also write to role_assignments.
-- A follow-up migration will drop them.
-- =====================================================================

SET @dbname = DATABASE();

-- Detect UUID column type from roles.id (BINARY(16) vs CHAR(36)).
SELECT @uuid_type := COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @dbname
  AND TABLE_NAME = 'roles'
  AND COLUMN_NAME = 'id';

SET @id_type = IF(LOWER(@uuid_type) LIKE 'binary(%', 'BINARY(16)', 'CHAR(36)');

-- ---------------------------------------------------------------------
-- role_scopes (applicableScopes ElementCollection)
-- ---------------------------------------------------------------------
SELECT @role_scopes_exists := COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'role_scopes';

SET @sql = IF(@role_scopes_exists = 0,
  CONCAT(
    'CREATE TABLE role_scopes (',
    '  role_id ', @id_type, ' NOT NULL,',
    '  scope_type VARCHAR(64) NOT NULL,',
    '  PRIMARY KEY (role_id, scope_type),',
    '  KEY idx_role_scopes_scope_type (scope_type),',
    '  CONSTRAINT fk_role_scopes_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE',
    ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci'
  ),
  'SELECT "role_scopes already exists" AS message'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- role_assignments
-- ---------------------------------------------------------------------
SELECT @role_assignments_exists := COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'role_assignments';

SET @sql = IF(@role_assignments_exists = 0,
  CONCAT(
    'CREATE TABLE role_assignments (',
    '  id ', @id_type, ' NOT NULL,',
    '  created_date TIMESTAMP NULL,',
    '  updated_date TIMESTAMP NULL,',
    '  created_by VARCHAR(255) NULL,',
    '  updated_by VARCHAR(255) NULL,',
    '  tenant_id VARCHAR(255) NULL,',
    '  user_id ', @id_type, ' NOT NULL,',
    '  role_id ', @id_type, ' NOT NULL,',
    '  scope_type VARCHAR(64) NOT NULL,',
    '  scope_id ', @id_type, ' NULL,',
    '  assigned_by VARCHAR(255) NULL,',
    '  expires_at TIMESTAMP NULL,',
    '  active TINYINT(1) NOT NULL DEFAULT 1,',
    '  PRIMARY KEY (id),',
    '  UNIQUE KEY uk_role_assignments_user_role_scope (user_id, role_id, scope_type, scope_id),',
    '  KEY idx_role_assignments_user_active (user_id, active),',
    '  KEY idx_role_assignments_scope (scope_type, scope_id, role_id),',
    '  KEY idx_role_assignments_user_scope_type (user_id, scope_type),',
    '  CONSTRAINT fk_role_assignments_user FOREIGN KEY (user_id) REFERENCES users(id),',
    '  CONSTRAINT fk_role_assignments_role FOREIGN KEY (role_id) REFERENCES roles(id)',
    ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci'
  ),
  'SELECT "role_assignments already exists" AS message'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- Backfill role_scopes for known role names (mirrors
-- RoleService.defaultApplicableScopesFor).
-- ---------------------------------------------------------------------

-- Universal roles can be granted at any scope
INSERT IGNORE INTO role_scopes (role_id, scope_type)
SELECT r.id, s.scope_type
FROM roles r
CROSS JOIN (
    SELECT 'GLOBAL'     AS scope_type
    UNION ALL SELECT 'TENANT'
    UNION ALL SELECT 'B2B_UNIT'
    UNION ALL SELECT 'STORE'
    UNION ALL SELECT 'PROJECT'
    UNION ALL SELECT 'WAREHOUSE'
    UNION ALL SELECT 'DEPARTMENT'
    UNION ALL SELECT 'TEAM'
) s
WHERE r.name IN ('ROLE_USER', 'ADMIN', 'ROLE_ADMIN');

-- Business / HR admin roles are granted at the B2B_UNIT scope
INSERT IGNORE INTO role_scopes (role_id, scope_type)
SELECT r.id, 'B2B_UNIT'
FROM roles r
WHERE r.name IN ('BUSINESS_ADMIN', 'ROLE_BUSINESS_ADMIN',
                 'HR_ADMIN', 'ROLE_HR_ADMIN');

-- Store roles → STORE scope
INSERT IGNORE INTO role_scopes (role_id, scope_type)
SELECT r.id, 'STORE'
FROM roles r
WHERE r.name IN ('STORE_ADMIN', 'ROLE_STORE_ADMIN',
                 'STORE_MANAGER', 'ROLE_STORE_MANAGER',
                 'STORE_STAFF',   'ROLE_STORE_STAFF');

-- Project roles → PROJECT scope
INSERT IGNORE INTO role_scopes (role_id, scope_type)
SELECT r.id, 'PROJECT'
FROM roles r
WHERE r.name IN ('PROJECT_MANAGER', 'ROLE_PROJECT_MANAGER',
                 'TEAM_LEAD',       'ROLE_TEAM_LEAD',
                 'DEVELOPER',       'ROLE_DEVELOPER',
                 'QA', 'DESIGNER', 'ANALYST', 'DEVOPS');

-- Warehouse roles → WAREHOUSE scope
INSERT IGNORE INTO role_scopes (role_id, scope_type)
SELECT r.id, 'WAREHOUSE'
FROM roles r
WHERE r.name IN ('WAREHOUSE_ADMIN', 'ROLE_WAREHOUSE_ADMIN',
                 'WAREHOUSE_STAFF', 'ROLE_WAREHOUSE_STAFF');

-- Catch-all: any role still without a scope entry may be assigned at B2B_UNIT
-- (the most common enterprise scope). Ops team can curate afterwards.
INSERT IGNORE INTO role_scopes (role_id, scope_type)
SELECT r.id, 'B2B_UNIT'
FROM roles r
LEFT JOIN role_scopes rs ON rs.role_id = r.id
WHERE rs.role_id IS NULL;

-- ---------------------------------------------------------------------
-- Backfill role_assignments from legacy junction tables.
--
-- We duplicate the data instead of moving it so existing readers of
-- business_user_roles / project_user_roles keep working while the services
-- are migrated. A follow-up migration will drop the legacy tables.
-- ---------------------------------------------------------------------

-- business_user_roles → role_assignments (scope=B2B_UNIT)
SELECT @has_bur := COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'business_user_roles';

SET @sql = IF(@has_bur > 0,
  CONCAT(
    'INSERT IGNORE INTO role_assignments ',
    '(id, created_date, updated_date, created_by, updated_by, tenant_id, ',
    ' user_id, role_id, scope_type, scope_id, assigned_by, active) ',
    'SELECT bur.id, bur.created_date, bur.updated_date, bur.created_by, bur.updated_by, bur.tenant_id, ',
    '       bur.user_id, bur.role_id, ''B2B_UNIT'', bur.business_id, bur.assigned_by, 1 ',
    'FROM business_user_roles bur'
  ),
  'SELECT "business_user_roles table not present; skipping backfill" AS message'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- project_user_roles → role_assignments (scope=PROJECT)
SELECT @has_pur := COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'project_user_roles';

SET @sql = IF(@has_pur > 0,
  CONCAT(
    'INSERT IGNORE INTO role_assignments ',
    '(id, created_date, updated_date, created_by, updated_by, tenant_id, ',
    ' user_id, role_id, scope_type, scope_id, assigned_by, active) ',
    'SELECT pur.id, pur.created_date, pur.updated_date, pur.created_by, pur.updated_by, pur.tenant_id, ',
    '       pur.user_id, pur.role_id, ''PROJECT'', pur.project_id, pur.assigned_by, 1 ',
    'FROM project_user_roles pur'
  ),
  'SELECT "project_user_roles table not present; skipping backfill" AS message'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Global (unscoped) roles on the users.roles ManyToMany → role_assignments (scope=GLOBAL)
SELECT @has_ur := COUNT(*)
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'user_roles';

SET @sql = IF(@has_ur > 0,
  CONCAT(
    'INSERT IGNORE INTO role_assignments ',
    '(id, tenant_id, user_id, role_id, scope_type, scope_id, active) ',
    'SELECT ',
    CASE WHEN @id_type = 'BINARY(16)'
         THEN ' UNHEX(REPLACE(UUID(), ''-'', '''')), '
         ELSE ' UUID(), '
    END,
    ' u.tenant_id, ur.user_id, ur.role_id, ''GLOBAL'', NULL, 1 ',
    'FROM user_roles ur JOIN users u ON u.id = ur.user_id'
  ),
  'SELECT "user_roles table not present; skipping backfill" AS message'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
