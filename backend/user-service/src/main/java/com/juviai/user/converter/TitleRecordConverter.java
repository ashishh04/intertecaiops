package com.juviai.user.converter;

import com.juviai.user.converter.populator.TitleRecordPopulator;
import com.juviai.user.domain.TitleRecord;
import com.juviai.user.dto.TitleRecordData;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class TitleRecordConverter extends AbstractPopulatingConverter<TitleRecord, TitleRecordData> {
    public TitleRecordConverter(List<TitleRecordPopulator> populators) {
        super(populators, TitleRecordData.class);
    }
}
