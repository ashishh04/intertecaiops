package com.juviai.leave.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Org-specific public and restricted holiday calendar.
 * Used to exclude non-working days from leave day calculations.
 */
@Entity
@Table(name = "public_holidays")
@Getter
@Setter
@NoArgsConstructor
public class PublicHoliday extends BaseEntity {

    @Column(name = "b2b_unit_id", nullable = false)
    private UUID b2bUnitId;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "holiday_type", nullable = false, length = 32)
    private HolidayType holidayType = HolidayType.PUBLIC;

    /** NULL = applies to all regions in the org */
    @Column(length = 64)
    private String region;

    public PublicHoliday(UUID b2bUnitId, LocalDate holidayDate, String name,
                         HolidayType holidayType, String region) {
        this.b2bUnitId   = b2bUnitId;
        this.holidayDate = holidayDate;
        this.name        = name;
        this.holidayType = holidayType;
        this.region      = region;
    }
}
