package com.juviai.user.converter.populator.compensation;

import com.juviai.user.converter.populator.CompensationPopulator;
import com.juviai.user.domain.Compensation;
import com.juviai.user.dto.CompensationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompensationBasicPopulator implements CompensationPopulator {

    @Override
    public void populate(Compensation source, CompensationDto target) {
        if (source == null || target == null) {
            return;
        }
        target.setId(source.getId());
        target.setType(source.getType());
        target.setAmount(source.getAmount());
        target.setEffectiveStartDate(source.getEffectiveStartDate());
        target.setEffectiveEndDate(source.getEffectiveEndDate());
        target.setActive(source.isActive());
    }
}
