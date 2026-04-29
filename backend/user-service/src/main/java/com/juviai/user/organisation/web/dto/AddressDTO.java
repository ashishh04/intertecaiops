package com.juviai.user.organisation.web.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class AddressDTO {
    private UUID id;
    private String name;
    private String addressType;
    private String mobileNumber;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String fullText; // optional
}
