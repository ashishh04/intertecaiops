package com.juviai.user.converter.populator.profile;

import com.juviai.user.converter.populator.UserSkillPopulator;
import com.juviai.user.domain.UserSkill;
import com.juviai.user.dto.UserSkillData;
import org.springframework.stereotype.Component;

@Component
public class UserSkillBasicPopulator implements UserSkillPopulator {
    @Override
    public void populate(UserSkill source, UserSkillData target) {
        target.setId(source.getId());
        target.setUserId(source.getUserId());
        target.setName(source.getName());
        target.setLevel(source.getLevel());
    }
}
