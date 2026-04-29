package com.juviai.user.converter.populator.experience;

import com.juviai.user.converter.populator.ExperienceDetailsResponsePopulator;
import com.juviai.user.domain.Experience;
import com.juviai.user.web.dto.ExperienceB2BUnitDTO;
import com.juviai.user.web.dto.ExperienceDetailsResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ExperienceDetailsBasicPopulator implements ExperienceDetailsResponsePopulator {

    @Override
    public void populate(Experience source, ExperienceDetailsResponseDTO target) {
        if (source == null || target == null) {
            return;
        }
        if (source.getB2bUnit() != null && source.getB2bUnit().getId() != null) {
            ExperienceB2BUnitDTO bu = target.getB2bUnit();
            if (bu == null) {
                bu = new ExperienceB2BUnitDTO();
                target.setB2bUnit(bu);
            }
            bu.setId(source.getB2bUnit().getId());
        }
        target.setDepartmentId(source.getDepartment() != null ? source.getDepartment().getId() : null);
        target.setDepartmentCode(source.getDepartment() != null ? source.getDepartment().getCode() : null);
        target.setStartYear(source.getStartYear());
        target.setEndYear(source.getEndYear());
    }
}
