package com.juviai.user.organisation.domain;

import com.juviai.common.orm.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "b2b_unit_category",
        indexes = {
                @Index(name = "idx_b2b_unit_category_name_tenant", columnList = "name, tenant_id"),
                @Index(name = "idx_b2b_unit_category_code_tenant", columnList = "code, tenant_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class B2BUnitCategory extends BaseEntity {

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;
}
