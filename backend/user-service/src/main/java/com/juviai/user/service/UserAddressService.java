package com.juviai.user.service;

import com.juviai.common.tenant.TenantContext;
import com.juviai.user.domain.User;
import com.juviai.user.organisation.domain.Address;
import com.juviai.user.organisation.repo.AddressRepository;
import com.juviai.user.organisation.service.CityService;
import com.juviai.user.organisation.service.StateService;
import com.juviai.user.repo.UserRepository;
import com.juviai.user.web.dto.CreateUserAddressRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final CityService cityService;
    private final StateService stateService;

    private String currentTenant() {
        return Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
    }

    @Transactional
    public Address createForUser(@NonNull UUID userId, @NonNull CreateUserAddressRequest request) {
        String tenantId = currentTenant();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getMobile() != null && !request.getMobile().isBlank()) {
            user.setMobile(request.getMobile().trim());
        }
        if (request.getLinkedinProfile() != null) {
            user.setLinkedinProfile(request.getLinkedinProfile());
        }
        userRepository.save(user);

        Address address = new Address();
        address.setTenantId(tenantId);
        address.setUser(user);
        address.setName(request.getName());
        address.setAddressType(request.getAddressType());
        address.setMobileNumber(request.getMobileNumber());
        address.setLine1(request.getAddressLine1());
        address.setLine2(request.getAddressLine2());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setFullText(request.getFullText());
        address.setState(stateService.getById(request.getStateId()));
        address.setCity(cityService.getById(request.getCityId()));

        return addressRepository.save(address);
    }

    @Transactional(readOnly = true)
    public Page<Address> listForUser(@NonNull UUID userId, @NonNull Pageable pageable) {
        return addressRepository.findByUser_IdAndTenantId(userId, currentTenant(), pageable);
    }
}
