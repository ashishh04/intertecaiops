package com.juviai.user.converter.populator.employee;

import com.juviai.user.converter.populator.EmployeeDetailsPopulator;
import com.juviai.user.domain.Employee;
import com.juviai.user.dto.EmployeeDetailsDto;
import org.springframework.stereotype.Component;

@Component
public class EmployeeDetailsBasicPopulator implements EmployeeDetailsPopulator {

    @Override
    public void populate(Employee source, EmployeeDetailsDto target) {
        target.setId(source.getId());
        target.setB2bUnitId(source.getB2bUnit() != null ? source.getB2bUnit().getId() : null);
        target.setStoreId(source.getStoreId());

        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setEmail(source.getEmail());
        target.setMobile(source.getMobile());
        target.setEmployeeCode(source.getEmployeeCode());
        target.setDismissed(source.getDismissed());

        target.setDateOfBirth(source.getDateOfBirth());
        target.setGender(source.getGender());
        target.setAnnualSalary(source.getAnnualSalary());
        target.setPanNumber(source.getPanNumber());
        target.setPfNumber(source.getPfNumber());
        target.setUanNumber(source.getUanNumber());
        target.setPfEnabled(source.getPfEnabled());
        if (source.getDesignation() != null) {
            target.setDesignationId(source.getDesignation().getId());
            target.setDesignation(source.getDesignation().getName());
        }

        if (source.getDepartment() != null) {
            target.setDepartmentId(source.getDepartment().getId());
            target.setDepartment(source.getDepartment().getName());
        }

        if (source.getBand() != null) {
            target.setBandId(source.getBand().getId());
            target.setBand(source.getBand().getName());
        }

        target.setHireDate(source.getHireDate());
        target.setEmploymentType(source.getEmploymentType());
    }
}
