package com.juviai.user.organisation.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "holiday_calendar",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_holiday_calendar_city_tenant", columnNames = {"city_id", "tenant_id"})
        },
        indexes = {
                @Index(name = "idx_holiday_calendar_b2b_tenant", columnList = "b2b_unit_id, tenant_id"),
                @Index(name = "idx_holiday_calendar_city_tenant", columnList = "city_id, tenant_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class HolidayCalendar extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "b2b_unit_id", nullable = false)
    private B2BUnit b2bUnit;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;
}
