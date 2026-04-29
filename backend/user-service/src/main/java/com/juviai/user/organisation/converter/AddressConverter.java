package com.juviai.user.organisation.converter;

import com.juviai.user.converter.AbstractPopulatingConverter;
import com.juviai.user.organisation.converter.populator.AddressPopulator;
import com.juviai.user.organisation.domain.Address;
import com.juviai.user.organisation.web.dto.AddressDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AddressConverter extends AbstractPopulatingConverter<Address, AddressDTO> {

    public AddressConverter(List<AddressPopulator> populators) {
        super(populators, AddressDTO.class);
    }
}
