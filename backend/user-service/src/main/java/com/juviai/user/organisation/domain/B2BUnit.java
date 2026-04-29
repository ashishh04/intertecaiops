package com.juviai.user.organisation.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.juviai.common.orm.BaseEntity;
import com.juviai.user.domain.User;
import com.juviai.common.crypto.EncryptedStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.juviai.user.domain.EmployeeOrgBand;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(
        name = "b2b_unit",
        indexes = {
                @Index(name = "idx_b2b_name_tenant", columnList = "name, tenant_id"),
                @Index(name = "idx_b2b_status", columnList = "status"),
                @Index(name = "idx_b2b_tenant", columnList = "tenant_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class B2BUnit extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = true, length = 300)
    private String brandTagLine;

    @Column(nullable = true, length = 500)
    private String startupDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private B2BUnitType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private B2BUnitStatus status = B2BUnitStatus.PENDING_APPROVAL;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 512)
    private String contactEmail;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 512)
    private String contactPhone;

    @Column(length = 255)
    private String website;

    @Column(name = "company_code", length = 64)
    private String companyCode;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "tan_number", length = 512)
    private String tanNumber;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "cin_number", length = 512)
    private String cinNumber;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "gst_number", length = 512)
    private String gstNumber;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "pan_number", length = 512)
    private String panNumber;

    @Column(length = 200)
    private String targetAudience;

    @Column(length = 200)
    private String revenueModel;

    @Column(name = "logo", length = 512)
    private String logo;

    @OneToMany(mappedBy = "b2bUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses;

    @Column(name = "salary_date")
    private Integer salaryDate;

    @OneToMany(mappedBy = "b2bUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HolidayCalendar> holidayCalendars;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", unique = true)
    private B2BUnitCategory category;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private B2BGroup group;

    @ManyToMany
    @JoinTable(
            name = "b2b_unit_departments",
            joinColumns = @JoinColumn(name = "b2b_unit_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"b2b_unit_id", "department_id"})
    )
    @JsonIgnoreProperties("b2bUnits")
    private Set<Department> departments = new HashSet<>();

    @jakarta.persistence.ElementCollection
    @jakarta.persistence.CollectionTable(
            name = "b2b_unit_attributes",
            joinColumns = @JoinColumn(name = "b2b_unit_id")
    )
    @jakarta.persistence.MapKeyColumn(name = "attr_key", length = 100)
    @jakarta.persistence.Column(name = "attr_value", length = 512)
    private Map<String, String> additionalAttributes = new HashMap<>();

    // Helper method to add Department
    public void addDepartment(Department department) {
        this.departments.add(department);
        department.getB2bUnits().add(this);
    }

    public void addAddress(Address address) {
        if (address == null) return;
        if (this.addresses == null) {
            this.addresses = new java.util.ArrayList<>();
        }
        address.setB2bUnit(this);
        this.addresses.add(address);
    }

    public void removeAddress(Address address) {
        if (address == null || this.addresses == null) return;
        this.addresses.remove(address);
        address.setB2bUnit(null);
    }

    // Helper method to remove Department
    public void removeDepartment(Department department) {
        this.departments.remove(department);
        department.getB2bUnits().remove(this);
    }

    @OneToOne
    @JoinColumn(name = "onboarded_by_user_id")
    private User onboardedBy;

    @OneToOne
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    private Instant approvedAt;

    @OneToMany(mappedBy = "b2bUnit", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<EmployeeOrgBand> employeeBands = new HashSet<>();

    @Column
    private boolean studentStartup;

    @Column
    private boolean findCoFounder;

    @Column
    private boolean buildSolo;

    @Column
    private boolean inviteCoFounder;

    @Column
    private Boolean isStartup;

    @Column
    private Boolean isBootstrapped;
}
