package com.juviai.user.organisation.domain;

import com.juviai.common.orm.BaseEntity;
import com.juviai.user.domain.User;
import com.juviai.common.crypto.EncryptedStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "address")
@Getter
@Setter
@NoArgsConstructor
public class Address extends BaseEntity {

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 512)
    private String name;

    @Column(length = 32)
    private String addressType;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 512)
    private String mobileNumber;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 512)
    private String line1;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 512)
    private String line2;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne
    @JoinColumn(name = "state_id")
    private State state;

    @ManyToOne
    @JoinColumn(name = "b2b_unit_id")
    private B2BUnit b2bUnit;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 64)
    private String country;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 64)
    private String postalCode;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "full_text", columnDefinition = "TEXT")
    private String fullText;
}
