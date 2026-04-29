package com.juviai.user.organisation.converter.populator.address;

import com.juviai.user.organisation.converter.populator.AddressPopulator;
import com.juviai.user.organisation.domain.Address;
import com.juviai.user.organisation.web.dto.AddressDTO;
import org.springframework.stereotype.Component;

@Component
public class AddressBasicPopulator implements AddressPopulator {

    @Override
    public void populate(Address source, AddressDTO target) {
        if (source == null || target == null) return;
        target.setId(source.getId());
        target.setName(source.getName());
        target.setAddressType(source.getAddressType());
        target.setMobileNumber(source.getMobileNumber());
        target.setLine1(source.getLine1());
        target.setLine2(source.getLine2());
        target.setCity(source.getCity() != null ? source.getCity().getName() : null);
        target.setState(source.getState() != null ? source.getState().getName() : null);
        target.setCountry(source.getCountry());
        target.setPostalCode(source.getPostalCode());
        target.setFullText(source.getFullText());
    }
}
