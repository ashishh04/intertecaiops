package com.juviai.user.converter.populator.business;

import com.juviai.user.converter.populator.MyBusinessResponsePopulator;
import com.juviai.user.domain.User;
import com.juviai.user.web.dto.MyBusinessResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMyBusinessBasicPopulator implements MyBusinessResponsePopulator {

    @Override
    public void populate(User source, MyBusinessResponseDTO target) {
        target.setName(source.getFirstName()+' '+source.getLastName());
        target.setId(source.getId());
        target.setEmail(source.getEmail());
        target.setB2bUnitId(source.getB2bUnit() != null ? source.getB2bUnit().getId() : null);
        target.setStudent(source.isStudent());
    }
}
