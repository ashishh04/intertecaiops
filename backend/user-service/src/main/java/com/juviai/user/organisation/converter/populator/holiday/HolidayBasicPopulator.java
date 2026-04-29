package com.juviai.user.organisation.converter.populator.holiday;

import com.juviai.user.organisation.converter.populator.HolidayPopulator;
import com.juviai.user.organisation.domain.Holiday;
import com.juviai.user.organisation.web.dto.HolidayDTO;
import org.springframework.stereotype.Component;

@Component
public class HolidayBasicPopulator implements HolidayPopulator {

    @Override
    public void populate(Holiday source, HolidayDTO target) {
        if (source == null || target == null) return;
        target.setId(source.getId());
        target.setDate(source.getDate());
        target.setName(source.getName());
    }
}
