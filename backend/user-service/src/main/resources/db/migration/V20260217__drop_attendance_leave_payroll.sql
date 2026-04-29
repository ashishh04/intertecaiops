-- Remove attendance/leave/payroll (payslip) and salary component/structure tables (safe if already removed)
SET @dbname = DATABASE();

-- Drop tables (order matters because of foreign keys)
SET @sql = 'DROP TABLE IF EXISTS employee_payslip_component';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = 'DROP TABLE IF EXISTS employee_payslip';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = 'DROP TABLE IF EXISTS employee_salary_component';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = 'DROP TABLE IF EXISTS employee_salary_structure';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = 'DROP TABLE IF EXISTS salary_components';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = 'DROP TABLE IF EXISTS employee_leave_deduction';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = 'DROP TABLE IF EXISTS employee_leave';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = 'DROP TABLE IF EXISTS employee_attendance';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
