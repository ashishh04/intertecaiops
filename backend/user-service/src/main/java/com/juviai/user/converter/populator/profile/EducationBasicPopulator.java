package com.juviai.user.converter.populator.profile;

import com.juviai.user.converter.populator.EducationPopulator;
import com.juviai.user.domain.Education;
import com.juviai.user.dto.EducationData;
import org.springframework.stereotype.Component;

@Component
public class EducationBasicPopulator implements EducationPopulator {
    @Override
    public void populate(Education source, EducationData target) {
        target.setId(source.getId());
        target.setUserId(source.getUserId());
        target.setInstitution(source.getInstitution());
        target.setDegree(source.getDegree());
        target.setFieldOfStudy(source.getFieldOfStudy());
        target.setStartDate(source.getStartDate());
        target.setEndDate(source.getEndDate());
    }
}
