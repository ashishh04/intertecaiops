package com.juviai.user.converter;

import com.juviai.user.converter.populator.EducationPopulator;
import com.juviai.user.domain.Education;
import com.juviai.user.dto.EducationData;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class EducationConverter extends AbstractPopulatingConverter<Education, EducationData> {
    public EducationConverter(List<EducationPopulator> populators) {
        super(populators, EducationData.class);
    }
}
