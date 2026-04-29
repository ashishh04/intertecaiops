package com.juviai.payroll.domain;

public enum TaxSection {
    SECTION_80C,    // PF, ELSS, LIC, PPF, NSC, tuition fees
    SECTION_80D,    // Medical insurance premiums
    SECTION_80E,    // Education loan interest
    SECTION_24,     // Home loan interest
    HRA,            // House Rent Allowance exemption
    LTA,            // Leave Travel Allowance
    OTHERS
}
