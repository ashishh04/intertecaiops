package com.juviai.user.web;

import com.juviai.common.dto.UserDTO;
import com.juviai.user.organisation.converter.AddressConverter;
import com.juviai.user.organisation.repo.AddressRepository;
import com.juviai.user.organisation.web.dto.AddressDTO;
import com.juviai.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/internal")
@RequiredArgsConstructor
public class InternalLookupController {

    private final UserService userService;
    private final AddressRepository addressRepository;
    private final AddressConverter addressConverter;

    @GetMapping("/profile/{userId}")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> getUserProfile(@PathVariable("userId") @NonNull UUID userId) {
        var user = userService.getUserById(userId);
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        return Map.of(
                "id", dto.getId(),
                "firstName", dto.getFirstName(),
                "lastName", dto.getLastName(),
                "email", dto.getEmail(),
                "mobile", user.getMobile()
        );
    }

    @GetMapping("/address/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public AddressDTO getAddressById(@PathVariable("addressId") @NonNull UUID addressId) {
        var address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found: " + addressId));
        return addressConverter.convert(address);
    }
}
