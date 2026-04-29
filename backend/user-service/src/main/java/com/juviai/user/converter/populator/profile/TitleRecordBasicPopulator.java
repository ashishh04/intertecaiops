package com.juviai.user.converter.populator.profile;

import com.juviai.user.converter.populator.TitleRecordPopulator;
import com.juviai.user.domain.TitleRecord;
import com.juviai.user.dto.TitleRecordData;
import org.springframework.stereotype.Component;

@Component
public class TitleRecordBasicPopulator implements TitleRecordPopulator {
    @Override
    public void populate(TitleRecord source, TitleRecordData target) {
        target.setId(source.getId());
        target.setUserId(source.getUserId());
        target.setTitle(source.getTitle());
        target.setStartDate(source.getStartDate());
        target.setEndDate(source.getEndDate());
    }
}
