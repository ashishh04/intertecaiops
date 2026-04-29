package com.juviai.user.service;

import com.juviai.user.domain.Designation;
import com.juviai.user.dto.DesignationDTO;
import com.juviai.user.dto.DesignationRequestDTO;
import com.juviai.user.dto.DesignationResponseDTO;
import com.juviai.user.repo.DesignationRepository;
import com.juviai.user.repo.EmployeeRepository;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DesignationService {

    private final DesignationRepository designationRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeBandService service;

    public DesignationService(DesignationRepository designationRepository,
                              EmployeeRepository employeeRepository, EmployeeBandService service) {
        this.designationRepository = designationRepository;
        this.employeeRepository = employeeRepository;
        this.service = service;
    }
    public List<DesignationDTO> getDesignations(UUID b2bUnitId) {
        List<DesignationDTO> result = employeeRepository.findDesignationWithBandAndResourceCount(b2bUnitId);
        
        // If no designations are found with employees, return all designations for the b2bUnit
        if (result.isEmpty()) {
            List<Designation> designations = designationRepository.findAllByB2bUnitId(b2bUnitId);
            return designations.stream()
                .map(desig -> {
                    DesignationDTO dto = new DesignationDTO();
                    dto.setDesignationId(desig.getId());
                    dto.setDesignationName(desig.getName());
                    dto.setBandName(desig.getBand() != null ? desig.getBand().getName() : null);
                    dto.setResourceCount(0L); // Set to 0 since there are no employees
                    return dto;
                })
                .collect(Collectors.toList());
        }
        
        return result;
    }


    // List with band counts
    public List<DesignationResponseDTO> getAllDesignations() {
        List<Designation> designations = designationRepository.findAll();
        List<DesignationResponseDTO> result = new ArrayList<>();

        for (Designation desig : designations) {
            DesignationResponseDTO dto = new DesignationResponseDTO();
            dto.setDesignationId(desig.getId());
            dto.setDesignationName(desig.getName());
            result.add(dto);
        }

        return result;
    }

    // Create
    @SuppressWarnings("null")
	public Designation createDesignation(DesignationRequestDTO request) {
        if (designationRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Designation already exists");
        }
        Designation designation = new Designation();
        designation.setName(request.getName());
        if(request.getBandId() != null) {
            designation.setBand(service.getBand(request.getBandId()));
        }
        designation.setB2bUnitId(request.getB2bUnitId());
        return designationRepository.save(designation);
    }

    // Update
    @SuppressWarnings("null")
	public Designation updateDesignation(@NonNull UUID id, DesignationRequestDTO request) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Designation not found"));
        if (request.getName() != null) {
            designation.setName(request.getName());
        }
        if (request.getBandId() != null) {
            designation.setBand(service.getBand(request.getBandId()));
        }
        return designationRepository.save(designation);
    }

    // Delete
    public void deleteDesignation(@NonNull UUID id) {
        if (!designationRepository.existsById(id)) {
            throw new NoSuchElementException("Designation not found");
        }
        designationRepository.deleteById(id);
    }

    public Designation findById(@NonNull UUID designation) {
       return designationRepository.findById(designation)
                .orElseThrow(() -> new NoSuchElementException("Designation not found"));
    }

    public List<DesignationDTO> getAllDesignations(UUID b2bUnitId) {
        List<DesignationDTO> list=new ArrayList<>();
        List<Designation> desiList= designationRepository.findAllByB2bUnitId(b2bUnitId);
        for(Designation designation:desiList){
            DesignationDTO dto=new DesignationDTO();
            dto.setDesignationId(designation.getId());
            dto.setDesignationName(designation.getName());
            list.add(dto);
        }
        return list;
    }
}

