package com.juviai.payroll.domain;

public enum ComponentType {
    BASIC,
    HRA,
    SPECIAL_ALLOWANCE,
    CONVEYANCE_ALLOWANCE,
    MEDICAL_ALLOWANCE,
    LTA,                    // Leave Travel Allowance
    PF,                     // Provident Fund (deduction)
    PROFESSIONAL_TAX,       // deduction
    TDS,                    // Tax Deducted at Source (deduction)
    BONUS,
    INCENTIVE,
    REIMBURSEMENT,
    DEDUCTION               // generic deduction
}
