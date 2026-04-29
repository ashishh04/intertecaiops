package com.juviai.user.organisation.web.mapper;

import com.juviai.user.organisation.domain.Address;
import com.juviai.user.organisation.domain.B2BGroup;
import com.juviai.user.organisation.domain.B2BUnit;
import com.juviai.user.organisation.web.dto.AddressDTO;
import com.juviai.user.organisation.web.dto.B2BUnitDTO;

import java.util.List;

public class B2BUnitMapper {

    public static B2BUnitDTO toDTO(B2BUnit unit) {
        if (unit == null) return null;
        B2BUnitDTO dto = new B2BUnitDTO();
        dto.setId(unit.getId());
        dto.setName(unit.getName());
        dto.setType(unit.getType());
        dto.setStatus(unit.getStatus());
        dto.setContactEmail(unit.getContactEmail());
        dto.setContactPhone(unit.getContactPhone());
        dto.setWebsite(unit.getWebsite());
        dto.setLogo(unit.getLogo());
        dto.setAdditionalAttributes(unit.getAdditionalAttributes());
        if (unit.getAddresses() != null && !unit.getAddresses().isEmpty()) {
            List<AddressDTO> addrs = unit.getAddresses().stream().map(B2BUnitMapper::toDTO).toList();
            dto.setAddresses(addrs);
            dto.setAddress(addrs.get(0));
        }
        B2BGroup group = unit.getGroup();
        if (group != null) {
            dto.setGroupId(group.getId());
            dto.setGroupName(group.getName());
        }
        return dto;
    }

    public static B2BUnitDTO toDTOList(B2BUnit unit) {
        if (unit == null) return null;
        B2BUnitDTO dto = new B2BUnitDTO();
        dto.setId(unit.getId());
        dto.setName(unit.getName());
        dto.setType(unit.getType());
        dto.setStatus(unit.getStatus());
        dto.setWebsite(unit.getWebsite());
        dto.setLogo(unit.getLogo());
        dto.setAdditionalAttributes(unit.getAdditionalAttributes());
        return dto;
    }

    private static AddressDTO toDTO(Address address) {
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
