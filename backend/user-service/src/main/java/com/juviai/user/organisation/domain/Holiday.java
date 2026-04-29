package com.juviai.user.organisation.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "holidays",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_holiday_calendar_date_tenant", columnNames = {"holiday_calendar_id", "holiday_date", "tenant_id"})
        },
        indexes = {
                @Index(name = "idx_holiday_calendar_tenant", columnList = "holiday_calendar_id, tenant_id"),
                @Index(name = "idx_holiday_date_tenant", columnList = "holiday_date, tenant_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Holiday extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holiday_calendar_id", nullable = false)
    private HolidayCalendar holidayCalendar;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 200)
    private String name;
}
