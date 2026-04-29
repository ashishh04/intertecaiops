-- ============================================================
-- V1__create_leave_schema.sql
-- Creates the full leave management schema:
--   leave_types, leave_policies, leave_balances, leave_requests,
--   leave_request_days, public_holidays, comp_offs
-- ============================================================

-- ── Leave Types ───────────────────────────────────────────────────────────────
-- Master list of leave types (e.g. Casual, Sick, Earned, Maternity).
CREATE TABLE IF NOT EXISTS leave_types (
    id                      CHAR(36)     NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    b2b_unit_id             CHAR(36)     NOT NULL,
    code                    VARCHAR(32)  NOT NULL,             -- CL, SL, EL, ML, PL
    name                    VARCHAR(128) NOT NULL,             -- Casual Leave, Sick Leave
    description             TEXT         NULL,
    is_paid                 TINYINT(1)   NOT NULL DEFAULT 1,
    requires_document       TINYINT(1)   NOT NULL DEFAULT 0,   -- Medical cert for SL > 2 days
    max_consecutive_days    SMALLINT     NULL,
    carry_forward_allowed   TINYINT(1)   NOT NULL DEFAULT 0,
    max_carry_forward_days  SMALLINT     NOT NULL DEFAULT 0,
    encashable              TINYINT(1)   NOT NULL DEFAULT 0,
    active                  TINYINT(1)   NOT NULL DEFAULT 1,
    created_at              DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uq_leave_type (b2b_unit_id, code)
) ENGINE=InnoDB;

-- ── Leave Policies ────────────────────────────────────────────────────────────
-- Defines allocation rules per leave type per role/grade/region.
-- E.g. CL: 12 days/year for all, ML: 26 weeks for female employees.
CREATE TABLE IF NOT EXISTS leave_policies (
    id              CHAR(36)     NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    leave_type_id   CHAR(36)     NOT NULL,
    b2b_unit_id     CHAR(36)     NOT NULL,
    applicable_to   VARCHAR(32)  NOT NULL DEFAULT 'ALL',  -- ALL | GENDER_FEMALE | ROLE_SPECIFIC
    gender          VARCHAR(16)  NULL,                     -- MALE | FEMALE | NULL = all
    days_per_year   DECIMAL(5,2) NOT NULL,
    accrual_type    VARCHAR(32)  NOT NULL DEFAULT 'UPFRONT', -- UPFRONT | MONTHLY
    min_tenure_days SMALLINT     NOT NULL DEFAULT 0,        -- eligible after X days
    active          TINYINT(1)   NOT NULL DEFAULT 1,
    effective_from  DATE         NOT NULL,
    effective_to    DATE         NULL,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(id) ON DELETE CASCADE,
    INDEX idx_lp_leave_type (leave_type_id),
    INDEX idx_lp_b2b        (b2b_unit_id)
) ENGINE=InnoDB;

-- ── Leave Balances ────────────────────────────────────────────────────────────
-- Current leave balance per employee per leave type per year.
-- Updated on: application approval, cancellation, carry-forward, accrual.
CREATE TABLE IF NOT EXISTS leave_balances (
    id              CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    employee_id     CHAR(36)      NOT NULL,
    leave_type_id   CHAR(36)      NOT NULL,
    year            SMALLINT      NOT NULL,
    allocated_days  DECIMAL(5,2)  NOT NULL DEFAULT 0.00,
    used_days       DECIMAL(5,2)  NOT NULL DEFAULT 0.00,
    pending_days    DECIMAL(5,2)  NOT NULL DEFAULT 0.00,   -- applied but not yet approved
    carried_forward DECIMAL(5,2)  NOT NULL DEFAULT 0.00,
    lop_days        DECIMAL(5,2)  NOT NULL DEFAULT 0.00,   -- Loss of Pay days this year
    created_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(id),
    UNIQUE KEY uq_leave_balance (employee_id, leave_type_id, year),
    INDEX idx_lb_employee (employee_id)
) ENGINE=InnoDB;

-- ── Leave Requests ────────────────────────────────────────────────────────────
-- A single leave application by an employee.
CREATE TABLE IF NOT EXISTS leave_requests (
    id              CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    employee_id     CHAR(36)      NOT NULL,
    leave_type_id   CHAR(36)      NOT NULL,
    from_date       DATE          NOT NULL,
    to_date         DATE          NOT NULL,
    total_days      DECIMAL(5,2)  NOT NULL,                -- excludes weekends & holidays
    half_day        TINYINT(1)    NOT NULL DEFAULT 0,
    half_day_period VARCHAR(8)    NULL,                    -- MORNING | AFTERNOON
    reason          TEXT          NULL,
    document_url    VARCHAR(512)  NULL,
    status          VARCHAR(32)   NOT NULL DEFAULT 'PENDING',  -- PENDING | APPROVED | REJECTED | CANCELLED | REVOKED
    approved_by     CHAR(36)      NULL,
    approved_at     DATETIME(6)   NULL,
    rejection_reason TEXT         NULL,
    applied_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(id),
    INDEX idx_lr_employee   (employee_id),
    INDEX idx_lr_status     (status),
    INDEX idx_lr_dates      (from_date, to_date)
) ENGINE=InnoDB;

-- ── Leave Request Days ────────────────────────────────────────────────────────
-- Exploded day-level breakdown of a leave request (excludes weekends and holidays).
-- Used for accurate balance deduction and LOP calculation for payroll.
CREATE TABLE IF NOT EXISTS leave_request_days (
    id                CHAR(36)     NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    leave_request_id  CHAR(36)     NOT NULL,
    leave_date        DATE         NOT NULL,
    day_fraction      DECIMAL(3,2) NOT NULL DEFAULT 1.00,  -- 0.5 for half-day, 1.0 for full
    FOREIGN KEY (leave_request_id) REFERENCES leave_requests(id) ON DELETE CASCADE,
    UNIQUE KEY uq_lrd (leave_request_id, leave_date),
    INDEX idx_lrd_request (leave_request_id),
    INDEX idx_lrd_date    (leave_date)
) ENGINE=InnoDB;

-- ── Public Holidays ───────────────────────────────────────────────────────────
-- Public and restricted holidays per org unit and region.
CREATE TABLE IF NOT EXISTS public_holidays (
    id              CHAR(36)     NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    b2b_unit_id     CHAR(36)     NOT NULL,
    holiday_date    DATE         NOT NULL,
    name            VARCHAR(128) NOT NULL,
    holiday_type    VARCHAR(32)  NOT NULL DEFAULT 'PUBLIC',  -- PUBLIC | RESTRICTED | OPTIONAL
    region          VARCHAR(64)  NULL,    -- NULL = applies to all regions
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    UNIQUE KEY uq_holiday (b2b_unit_id, holiday_date, region),
    INDEX idx_ph_date (b2b_unit_id, holiday_date)
) ENGINE=InnoDB;

-- ── Comp Offs ─────────────────────────────────────────────────────────────────
-- Compensatory off credits earned when an employee works on a weekend or holiday.
CREATE TABLE IF NOT EXISTS comp_offs (
    id              CHAR(36)     NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    employee_id     CHAR(36)     NOT NULL,
    worked_date     DATE         NOT NULL,
    reason          VARCHAR(256) NOT NULL,
    credits         DECIMAL(3,2) NOT NULL DEFAULT 1.00,
    status          VARCHAR(32)  NOT NULL DEFAULT 'PENDING',  -- PENDING | APPROVED | REJECTED | CONSUMED
    approved_by     CHAR(36)     NULL,
    approved_at     DATETIME(6)  NULL,
    expires_at      DATE         NULL,    -- comp offs typically expire in 90 days
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_co_employee (employee_id),
    INDEX idx_co_status   (status)
) ENGINE=InnoDB;
