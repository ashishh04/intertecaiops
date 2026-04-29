package com.juviai.user.domain;

import com.juviai.common.orm.BaseEntity;
import com.juviai.common.crypto.EncryptedBigDecimalConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "compensations")
@Getter
@Setter
@NoArgsConstructor
public class Compensation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private CompensationType type;

    /**
     * Stored encrypted — salary figures are high-sensitivity financial PII.
     * NOTE: SQL-level aggregations (SUM/AVG/ORDER BY) on this column are not possible;
     * perform them in application code after decryption.
     * DB column must be VARCHAR(512) — change via migration if previously DECIMAL.
     */
    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "amount", nullable = false, length = 512)
    private BigDecimal amount;

    @Column(name = "effective_start_date", nullable = false)
    private LocalDate effectiveStartDate;

    @Column(name = "effective_end_date")
    private LocalDate effectiveEndDate;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
