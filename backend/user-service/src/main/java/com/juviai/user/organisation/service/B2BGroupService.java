package com.juviai.user.organisation.service;

import org.springframework.stereotype.Service;

import com.juviai.user.organisation.domain.B2BGroup;
import com.juviai.user.organisation.repo.B2BGroupRepository;

@Service
public class B2BGroupService {
	
	private B2BGroupRepository groupRepository;
	
	public B2BGroupService(B2BGroupRepository groupRepository) {
		this.groupRepository = groupRepository;
	}
	
	public B2BGroup findOrCreate(String code,String tenantId) {
		String normalized = code != null ? code.trim() : null;
		if (normalized == null || normalized.isBlank()) {
			throw new IllegalArgumentException("Group code is required");
		}
		return groupRepository
	        .findByCodeAndTenantId(normalized, tenantId)
	        .orElseGet(() -> {
	            B2BGroup g = new B2BGroup();
	            g.setCode(normalized);
	            g.setName(normalized);
	            g.setTenantId(tenantId);
	            return groupRepository.save(g);
	        });
	}
}
