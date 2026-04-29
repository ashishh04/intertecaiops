package com.juviai.user.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.juviai.common.orm.BaseEntity;
import com.juviai.user.organisation.domain.B2BUnit;
import com.juviai.user.organisation.domain.Address;

import com.juviai.common.crypto.EncryptedStringConverter;
import com.juviai.common.crypto.SearchableHashConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base user entity that represents a user in the system.
 * This is the parent class for all user types.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {
    
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    // Stored encrypted; emailHash used for login lookups
    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, length = 512)
    private String email;

    @Convert(converter = SearchableHashConverter.class)
    @Column(name = "email_hash", nullable = false, unique = true, length = 64)
    private String emailHash;

    // Stored encrypted; mobileHash used for OTP-login lookups
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 512)
    private String mobile;

    @Convert(converter = SearchableHashConverter.class)
    @Column(name = "mobile_hash", unique = true, length = 64)
    private String mobileHash;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, length = 512)
    private String firstName;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, length = 512)
    private String lastName;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Gender gender;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;
    
    @Column(nullable = false)
    private boolean passwordNeedsReset = false;

    @Column(nullable = false)
    private int tokenVersion = 0;

    @Column(length = 120)
    private String passwordSetupToken;

    private Instant passwordSetupTokenExpires;

    @Column(nullable = false)
    private boolean student = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @JsonManagedReference("user-roles")
    private Set<Role> roles = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "b2b_unit_id")
    private B2BUnit b2bUnit;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Address> addresses;

    @Column
    private String linkedinProfile;

    public User(String username, String email, String passwordHash, String firstName, String lastName, boolean active) {
        this.username = username != null ? username.toLowerCase() : null;
        setEmail(email);   // use setter to keep hash in sync
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
    }

    /**
     * Override Lombok-generated setter to keep emailHash in sync.
     * Always call this instead of setting email directly.
     */
    public void setEmail(String email) {
        String normalised = email != null ? email.toLowerCase().trim() : null;
        this.email = normalised;
        this.emailHash = SearchableHashConverter.hash(normalised);
    }

    /**
     * Override Lombok-generated setter to keep mobileHash in sync.
     */
    public void setMobile(String mobile) {
        this.mobile = mobile;
        this.mobileHash = SearchableHashConverter.hash(mobile);
    }

    // Role management methods
    public void addRole(Role role) {
        if (role != null) {
            if (this.roles == null) {
                this.roles = new HashSet<>();
            }
            this.roles.add(role);
            if (role.getUsers() != null) {
                role.getUsers().add(this);
            }
        }
    }

    public void removeRole(Role role) {
        if (role != null && this.roles != null) {
            this.roles.remove(role);
            if (role.getUsers() != null) {
                role.getUsers().remove(this);
            }
        }
    }

    public boolean hasRole(String roleName) {
        if (this.roles == null || roleName == null) {
            return false;
        }
        return this.roles.stream()
                .anyMatch(role -> roleName.equals(role.getName()));
    }
}
