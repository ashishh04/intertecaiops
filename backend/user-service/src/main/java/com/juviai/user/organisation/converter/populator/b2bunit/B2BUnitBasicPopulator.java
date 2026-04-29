package com.juviai.user.organisation.converter.populator.b2bunit;

import com.juviai.user.organisation.converter.populator.B2BUnitPopulator;
import com.juviai.user.organisation.domain.B2BUnit;
import com.juviai.user.organisation.web.dto.B2BUnitDTO;
import com.juviai.user.organisation.web.dto.AddressDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class B2BUnitBasicPopulator implements B2BUnitPopulator {

    @Override
    public void populate(B2BUnit source, B2BUnitDTO target) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setCompanyCode(source.getCompanyCode());
        target.setTanNumber(source.getTanNumber());
        target.setCinNumber(source.getCinNumber());
        target.setGstNumber(source.getGstNumber());
        target.setPanNumber(source.getPanNumber());
        target.setSalaryDate(source.getSalaryDate());
        target.setIsStartup(source.getIsStartup());
        target.setIsBootstrapped(source.getIsBootstrapped());
        target.setType(source.getType());
        target.setStatus(source.getStatus());
        target.setContactEmail(source.getContactEmail());
        target.setContactPhone(source.getContactPhone());
        target.setWebsite(source.getWebsite());
        target.setLogo(source.getLogo());
        target.setAdditionalAttributes(source.getAdditionalAttributes());
        if (source.getGroup() != null) {
            target.setGroupId(source.getGroup().getId());
            target.setGroupName(source.getGroup().getCode());
        }
        if (source.getAddresses() != null && !source.getAddresses().isEmpty()) {
            List<AddressDTO> addrs = source.getAddresses().stream().map(a -> {
                AddressDTO dto = new AddressDTO();
                dto.setLine1(a.getLine1());
                dto.setLine2(a.getLine2());
                dto.setCity(a.getCity() != null ? a.getCity().getName() : null);
                dto.setState(a.getState() != null ? a.getState().getName() : null);
                dto.setCountry(a.getCountry());
                dto.setPostalCode(a.getPostalCode());
                dto.setFullText(a.getFullText());
                return dto;
            }).toList();
            target.setAddresses(addrs);
            target.setAddress(addrs.getFirst());
        }
    }
}
