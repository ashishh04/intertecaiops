package com.juviai.user.organisation.web.dto;

import java.util.UUID;
import java.util.Map;
import java.util.List;

import com.juviai.user.organisation.domain.B2BUnitStatus;
import com.juviai.user.organisation.domain.B2BUnitType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class B2BUnitDTO {
    private UUID id;
    private String name;
    private String companyCode;
    private String tanNumber;
    private String cinNumber;
    private String gstNumber;
    private String panNumber;
    private Integer salaryDate;
    private Boolean isStartup;
    private Boolean isBootstrapped;
    private B2BUnitType type;
    private B2BUnitStatus status;
    private String contactEmail;
    private String contactPhone;
    private String website;
    private String logo;
    private Map<String, String> additionalAttributes;
    private AddressDTO address;
    private List<AddressDTO> addresses;
    private UUID groupId;
    private String groupName;
}
