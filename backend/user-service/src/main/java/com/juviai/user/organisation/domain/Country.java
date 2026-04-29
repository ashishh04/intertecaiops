package com.juviai.user.organisation.domain;

import com.juviai.common.orm.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
    name = "countries",
    indexes = {
        @Index(name = "idx_country_name_tenant", columnList = "name, tenant_id"),
        @Index(name = "idx_country_code_tenant", columnList = "code, tenant_id"),
        @Index(name = "idx_country_active_tenant", columnList = "active, tenant_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Country extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 32)
    private String code;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private boolean active = true;
}
