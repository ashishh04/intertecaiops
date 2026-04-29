package com.juviai.user.converter;

import com.juviai.user.converter.populator.ProfileExperiencePopulator;
import com.juviai.user.domain.ProfileExperience;
import com.juviai.user.dto.ProfileExperienceData;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ProfileExperienceConverter extends AbstractPopulatingConverter<ProfileExperience, ProfileExperienceData> {
    public ProfileExperienceConverter(List<ProfileExperiencePopulator> populators) {
        super(populators, ProfileExperienceData.class);
    }
}
