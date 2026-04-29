package com.juviai.user.converter.populator.employee;

import com.juviai.user.converter.populator.EmployeePopulator;
import com.juviai.user.domain.Employee;
import com.juviai.user.dto.EmployeeData;
import org.springframework.stereotype.Component;

@Component
public class EmployeeBasicPopulator implements EmployeePopulator {

    @Override
    public void populate(Employee source, EmployeeData target) {
        target.setId(source.getId());
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setEmail(source.getEmail());
        target.setMobile(source.getMobile());
        target.setStoreId(source.getStoreId());
        target.setEmployeeCode(source.getEmployeeCode());
        target.setDismissed(source.getDismissed());
        target.setHireDate(source.getHireDate());
        target.setEmploymentType(source.getEmploymentType());
        target.setCreatedDate(source.getCreatedDate());
        target.setUpdatedDate(source.getUpdatedDate());
        target.setDateOfBirth(source.getDateOfBirth());
        target.setGender(source.getGender());
        target.setAnnualSalary(source.getAnnualSalary());
        target.setPfNumber(source.getPfNumber());
        target.setUanNumber(source.getUanNumber());
        target.setPanNumber(source.getPanNumber());
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
        if (source.getReportingManager() != null) {
            target.setReportingManagerId(source.getReportingManager().getId());
            target.setReportingManagerName(
                    source.getReportingManager().getFirstName() + " " + source.getReportingManager().getLastName());
        }
    }
}
