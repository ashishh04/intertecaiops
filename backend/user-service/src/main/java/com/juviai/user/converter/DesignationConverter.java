package com.juviai.user.converter;

import com.juviai.user.converter.populator.DesignationPopulator;
import com.juviai.user.domain.Designation;
import com.juviai.user.dto.DesignationData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DesignationConverter extends AbstractPopulatingConverter<Designation, DesignationData> {

    public DesignationConverter(List<DesignationPopulator> populators) {
        super(populators, DesignationData.class);
    }
}
