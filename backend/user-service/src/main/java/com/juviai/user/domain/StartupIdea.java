package com.juviai.user.domain;

import com.juviai.common.orm.BaseEntity;
import com.juviai.user.organisation.domain.B2BUnitCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "startup_idea",
        indexes = {
                @Index(name = "idx_startup_idea_name_tenant", columnList = "name, tenant_id"),
                @Index(name = "idx_startup_idea_category_tenant", columnList = "category_id, tenant_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class StartupIdea extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "media_url", length = 512)
    private String mediaUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private B2BUnitCategory category;

    @Column(name = "number_of_likes", nullable = false)
    private long numberOfLikes = 0L;
}
