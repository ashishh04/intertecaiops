package com.juviai.user.converter;

import com.juviai.user.converter.populator.EmployeeBandPopulator;
import com.juviai.user.domain.EmployeeOrgBand;
import com.juviai.user.web.dto.EmployeeBandResponseDTO;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class EmployeeBandConverter extends AbstractPopulatingConverter<EmployeeOrgBand, EmployeeBandResponseDTO> {
    public EmployeeBandConverter(List<EmployeeBandPopulator> populators) {
        super(populators, EmployeeBandResponseDTO.class);
    }
}
