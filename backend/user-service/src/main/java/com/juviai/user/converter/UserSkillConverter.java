package com.juviai.user.converter;

import com.juviai.user.converter.populator.UserSkillPopulator;
import com.juviai.user.domain.UserSkill;
import com.juviai.user.dto.UserSkillData;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class UserSkillConverter extends AbstractPopulatingConverter<UserSkill, UserSkillData> {
    public UserSkillConverter(List<UserSkillPopulator> populators) {
        super(populators, UserSkillData.class);
    }
}
