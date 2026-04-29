package com.juviai.user.organisation.web.mapper;

import com.juviai.user.organisation.domain.Address;
import com.juviai.user.organisation.web.dto.AddressDTO;

public class OnboardingMapper {

    public static Address toEntity(AddressDTO dto) {
        if (dto == null) return null;
        Address addr = new Address();
        addr.setLine1(dto.getLine1());
        addr.setLine2(dto.getLine2());
        addr.setCountry(dto.getCountry());
        addr.setPostalCode(dto.getPostalCode());
        addr.setFullText(dto.getFullText());
        return addr;
    }
}
