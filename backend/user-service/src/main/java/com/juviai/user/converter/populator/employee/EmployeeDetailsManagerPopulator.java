package com.juviai.user.converter.populator.employee;

import com.juviai.user.converter.populator.EmployeeDetailsPopulator;
import com.juviai.user.domain.Employee;
import com.juviai.user.dto.EmployeeDetailsDto;
import com.juviai.user.dto.UserBriefDto;
import org.springframework.stereotype.Component;

@Component
public class EmployeeDetailsManagerPopulator implements EmployeeDetailsPopulator {

    @Override
    public void populate(Employee source, EmployeeDetailsDto target) {
        if (source.getReportingManager() == null) {
            target.setReportingManagerId(null);
            target.setReportingManager(null);
            return;
        }

        target.setReportingManagerId(source.getReportingManager().getId());
        target.setReportingManager(new UserBriefDto(
                source.getReportingManager().getId(),
                source.getReportingManager().getFirstName(),
                source.getReportingManager().getLastName()
        ));
    }
}
