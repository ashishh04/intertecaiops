-- ============================================================
-- V1__create_payroll_schema.sql
-- Creates the full payroll schema:
--   payroll_periods, payslips, payslip_components, tax_declarations,
--   salary_structures, salary_structure_components, reimbursements
-- ============================================================

-- ── Payroll Periods ──────────────────────────────────────────────────────────
-- Represents a payroll cycle (e.g. April 2026).
-- Each period is tied to a B2B unit (company/org unit).
CREATE TABLE IF NOT EXISTS payroll_periods (
    id             CHAR(36)     NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    b2b_unit_id    CHAR(36)     NOT NULL,
    period_year    SMALLINT     NOT NULL,                       -- e.g. 2026
    period_month   TINYINT      NOT NULL,                       -- 1–12
    status         VARCHAR(32)  NOT NULL DEFAULT 'DRAFT',       -- DRAFT | PROCESSING | FINALIZED | PAID
    payment_date   DATE         NULL,                            -- actual payment date
    remarks        TEXT         NULL,
    created_by     CHAR(36)     NULL,
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uq_payroll_period (b2b_unit_id, period_year, period_month)
) ENGINE=InnoDB;

-- ── Salary Structures ────────────────────────────────────────────────────────
-- Template that defines how a salary is broken into components for an employee.
CREATE TABLE IF NOT EXISTS salary_structures (
    id             CHAR(36)     NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    employee_id    CHAR(36)     NOT NULL,                       -- FK → users.id (Employee)
    name           VARCHAR(128) NOT NULL,                       -- e.g. "Senior Engineer Band A"
    effective_from DATE         NOT NULL,
    effective_to   DATE         NULL,                            -- NULL = currently active
    active         TINYINT(1)   NOT NULL DEFAULT 1,
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    INDEX idx_ss_employee (employee_id),
    INDEX idx_ss_active   (employee_id, active)
) ENGINE=InnoDB;

-- ── Salary Structure Components ──────────────────────────────────────────────
-- Individual earning/deduction lines inside a salary structure template.
CREATE TABLE IF NOT EXISTS salary_structure_components (
    id                   CHAR(36)     NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    salary_structure_id  CHAR(36)     NOT NULL,
    component_type       VARCHAR(32)  NOT NULL,   -- BASIC | HRA | SPECIAL_ALLOWANCE | PF | PROFESSIONAL_TAX | TDS | BONUS | DEDUCTION
    name                 VARCHAR(128) NOT NULL,   -- display name, e.g. "House Rent Allowance"
    calculation_type     VARCHAR(32)  NOT NULL,   -- FIXED | PERCENTAGE_OF_BASIC | PERCENTAGE_OF_GROSS
    value                DECIMAL(15,4) NOT NULL,  -- amount (FIXED) or percent (PERCENTAGE_*)
    is_taxable           TINYINT(1)   NOT NULL DEFAULT 1,
    is_earning           TINYINT(1)   NOT NULL DEFAULT 1,  -- 0 = deduction
    sort_order           SMALLINT     NOT NULL DEFAULT 0,
    created_at           DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    FOREIGN KEY (salary_structure_id) REFERENCES salary_structures(id) ON DELETE CASCADE,
    INDEX idx_ssc_structure (salary_structure_id)
) ENGINE=InnoDB;

-- ── Payslips ─────────────────────────────────────────────────────────────────
-- One payslip per employee per payroll period.
CREATE TABLE IF NOT EXISTS payslips (
    id                  CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    payroll_period_id   CHAR(36)      NOT NULL,
    employee_id         CHAR(36)      NOT NULL,
    employee_code       VARCHAR(64)   NULL,
    working_days        SMALLINT      NOT NULL DEFAULT 0,
    paid_days           SMALLINT      NOT NULL DEFAULT 0,
    lop_days            SMALLINT      NOT NULL DEFAULT 0,     -- Loss of Pay
    gross_earnings      DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_deductions    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    net_pay             DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status              VARCHAR(32)   NOT NULL DEFAULT 'DRAFT',  -- DRAFT | APPROVED | PAID | REVISED
    payment_reference   VARCHAR(128)  NULL,                       -- bank transfer ref
    generated_at        DATETIME(6)   NULL,
    paid_at             DATETIME(6)   NULL,
    created_at          DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    FOREIGN KEY (payroll_period_id) REFERENCES payroll_periods(id) ON DELETE RESTRICT,
    UNIQUE KEY uq_payslip (payroll_period_id, employee_id),
    INDEX idx_payslip_employee (employee_id),
    INDEX idx_payslip_status   (status)
) ENGINE=InnoDB;

-- ── Payslip Components ───────────────────────────────────────────────────────
-- Snapshot of every earning/deduction line on a payslip (immutable after APPROVED).
CREATE TABLE IF NOT EXISTS payslip_components (
    id               CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    payslip_id       CHAR(36)      NOT NULL,
    component_type   VARCHAR(32)   NOT NULL,
    name             VARCHAR(128)  NOT NULL,
    amount           DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    is_earning       TINYINT(1)    NOT NULL DEFAULT 1,
    sort_order       SMALLINT      NOT NULL DEFAULT 0,
    FOREIGN KEY (payslip_id) REFERENCES payslips(id) ON DELETE CASCADE,
    INDEX idx_pc_payslip (payslip_id)
) ENGINE=InnoDB;

-- ── Tax Declarations ─────────────────────────────────────────────────────────
-- Annual tax-saving declarations by the employee (80C, HRA, etc.).
CREATE TABLE IF NOT EXISTS tax_declarations (
    id              CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    employee_id     CHAR(36)      NOT NULL,
    financial_year  VARCHAR(10)   NOT NULL,   -- e.g. "2025-26"
    section         VARCHAR(32)   NOT NULL,   -- SECTION_80C | HRA | SECTION_80D | SECTION_24 | OTHERS
    description     VARCHAR(256)  NOT NULL,
    declared_amount DECIMAL(15,2) NOT NULL,
    approved_amount DECIMAL(15,2) NULL,
    status          VARCHAR(32)   NOT NULL DEFAULT 'PENDING',  -- PENDING | APPROVED | REJECTED
    document_url    VARCHAR(512)  NULL,
    reviewed_by     CHAR(36)      NULL,
    reviewed_at     DATETIME(6)   NULL,
    created_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    INDEX idx_td_employee (employee_id),
    INDEX idx_td_fy       (employee_id, financial_year)
) ENGINE=InnoDB;

-- ── Reimbursements ───────────────────────────────────────────────────────────
-- Expense claims submitted by employees (travel, medical, internet, etc.).
CREATE TABLE IF NOT EXISTS reimbursements (
    id              CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    employee_id     CHAR(36)      NOT NULL,
    category        VARCHAR(64)   NOT NULL,   -- TRAVEL | MEDICAL | INTERNET | FOOD | OTHER
    description     VARCHAR(512)  NOT NULL,
    claim_amount    DECIMAL(15,2) NOT NULL,
    approved_amount DECIMAL(15,2) NULL,
    receipt_url     VARCHAR(512)  NULL,
    claim_date      DATE          NOT NULL,
    status          VARCHAR(32)   NOT NULL DEFAULT 'PENDING',  -- PENDING | APPROVED | REJECTED | PAID
    approved_by     CHAR(36)      NULL,
    approved_at     DATETIME(6)   NULL,
    paid_in_period  CHAR(36)      NULL,   -- FK → payroll_periods.id (settled in which period)
    remarks         TEXT          NULL,
    created_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    INDEX idx_reimb_employee (employee_id),
    INDEX idx_reimb_status   (status),
    FOREIGN KEY (paid_in_period) REFERENCES payroll_periods(id) ON DELETE SET NULL
) ENGINE=InnoDB;
