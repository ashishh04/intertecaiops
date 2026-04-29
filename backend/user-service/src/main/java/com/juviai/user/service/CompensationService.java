package com.juviai.user.service;

import com.juviai.user.domain.Compensation;
import com.juviai.user.domain.CompensationType;
import java.util.List;
import java.util.UUID;

public interface CompensationService {

    List<Compensation> listHistory(UUID employeeId);

    Compensation getById(UUID id);

    List<Compensation> getAll();

    Compensation getActive(UUID employeeId);

    boolean hasAny(UUID employeeId);

    boolean hasAny(UUID employeeId, CompensationType type);

    Compensation getActive(UUID employeeId, CompensationType type);

    Compensation create(Compensation compensation);

    Compensation reviseActiveCompensation(UUID employeeId,
                                         CompensationType type,
                                         Compensation nextCompensation);
}
