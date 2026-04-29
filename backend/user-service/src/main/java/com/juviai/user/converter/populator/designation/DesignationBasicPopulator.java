package com.juviai.user.converter.populator.designation;

import com.juviai.user.converter.populator.DesignationPopulator;
import com.juviai.user.domain.Designation;
import com.juviai.user.dto.DesignationData;
import org.springframework.stereotype.Component;

@Component
public class DesignationBasicPopulator implements DesignationPopulator {

    @Override
    public void populate(Designation source, DesignationData target) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setB2bUnitId(source.getB2bUnitId());
        if (source.getBand() != null) {
            target.setBandId(source.getBand().getId());
            target.setBandName(source.getBand().getName());
        }
    }
}
