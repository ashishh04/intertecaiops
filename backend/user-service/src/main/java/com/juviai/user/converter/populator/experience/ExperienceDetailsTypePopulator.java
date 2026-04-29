package com.juviai.user.converter.populator.experience;

import com.juviai.user.converter.populator.ExperienceDetailsResponsePopulator;
import com.juviai.user.domain.Experience;
import com.juviai.user.organisation.domain.B2BUnitType;
import com.juviai.user.organisation.repo.B2BUnitRepository;
import com.juviai.user.web.dto.ExperienceB2BUnitDTO;
import com.juviai.user.web.dto.ExperienceDetailsResponseDTO;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ExperienceDetailsTypePopulator implements ExperienceDetailsResponsePopulator {

    private final B2BUnitRepository b2bUnitRepository;

    public ExperienceDetailsTypePopulator(B2BUnitRepository b2bUnitRepository) {
        this.b2bUnitRepository = b2bUnitRepository;
    }

    @Override
    public void populate(Experience source, ExperienceDetailsResponseDTO target) {
        if (source == null || target == null) {
            return;
        }

        UUID b2bUnitId = (source.getB2bUnit() != null) ? source.getB2bUnit().getId() : null;
        if (b2bUnitId != null) {
            b2bUnitRepository.findById(b2bUnitId).ifPresent(unit -> {
                ExperienceB2BUnitDTO bu = target.getB2bUnit();
                if (bu == null) {
                    bu = new ExperienceB2BUnitDTO();
                    target.setB2bUnit(bu);
                }
                bu.setId(unit.getId());
                bu.setName(unit.getName());
                bu.setType(unit.getType());
            });
        }

        String experienceType = "PROFESSIONAL";
        B2BUnitType t = (target.getB2bUnit() != null) ? target.getB2bUnit().getType() : null;
        if (t == B2BUnitType.COLLEGE) {
                experienceType = "ACADEMIC";
        }

        target.setExperienceType(experienceType);
    }
}
