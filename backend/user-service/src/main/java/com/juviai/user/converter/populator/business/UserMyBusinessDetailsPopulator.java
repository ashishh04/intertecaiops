package com.juviai.user.converter.populator.business;

import com.juviai.user.converter.populator.MyBusinessResponsePopulator;
import com.juviai.user.domain.User;
import com.juviai.user.organisation.converter.B2BUnitConverter;
import com.juviai.user.organisation.service.B2BUnitService;
import com.juviai.user.web.dto.MyBusinessResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserMyBusinessDetailsPopulator implements MyBusinessResponsePopulator {

    private final B2BUnitService b2bUnitService;
    private final B2BUnitConverter b2bUnitConverter;

    @Override
    public void populate(User source, MyBusinessResponseDTO target) {
        UUID b2bUnitId = target.getB2bUnitId();
        if (Objects.nonNull(b2bUnitId)) {
            Object businessDetails = b2bUnitConverter.convert(b2bUnitService.findById(b2bUnitId));
            target.setBusiness(businessDetails);
        }
    }
}
