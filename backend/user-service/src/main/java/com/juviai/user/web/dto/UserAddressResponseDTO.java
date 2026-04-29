package com.juviai.user.web.dto;

import com.juviai.user.organisation.web.dto.AddressDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserAddressResponseDTO {

    private UUID userId;
    private String userName;
    private AddressDTO address;
}
