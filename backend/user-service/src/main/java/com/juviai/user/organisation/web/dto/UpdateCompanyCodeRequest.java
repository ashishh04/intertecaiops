package com.juviai.user.organisation.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCompanyCodeRequest {
    private String companyCode;
    private String tanNumber;
    private String cinNumber;
    private String gstNumber;
    private String panNumber;
    private Integer salaryDate;
    private Boolean isStartup;
    private Boolean isBootstrapped;
}
