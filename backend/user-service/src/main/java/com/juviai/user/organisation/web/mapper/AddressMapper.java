package com.juviai.user.organisation.web.mapper;

import com.juviai.user.organisation.domain.Address;
import com.juviai.user.organisation.web.dto.AddressDTO;

public class AddressMapper {

    public static AddressDTO toDto(Address address) {
        if (address == null) return null;
        AddressDTO dto = new AddressDTO();
        dto.setLine1(address.getLine1());
        dto.setLine2(address.getLine2());
        dto.setCity(address.getCity() != null ? address.getCity().getCode() : null);
        dto.setState(address.getState() != null ? address.getState().getCode() : null);
        dto.setCountry(address.getCountry());
        dto.setPostalCode(address.getPostalCode());
        dto.setFullText(address.getFullText());
        return dto;
    }
}
