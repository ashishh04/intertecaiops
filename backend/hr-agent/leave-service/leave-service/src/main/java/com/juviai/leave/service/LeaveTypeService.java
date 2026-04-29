package com.juviai.leave.service;

import com.juviai.leave.domain.LeaveType;

import java.util.List;
import java.util.UUID;

public interface LeaveTypeService {
    LeaveType create(LeaveType leaveType);
    LeaveType getById(UUID id);
    LeaveType getByCode(UUID b2bUnitId, String code);
    List<LeaveType> listActive(UUID b2bUnitId);
    LeaveType update(UUID id, LeaveType updated);
    void deactivate(UUID id);
}
