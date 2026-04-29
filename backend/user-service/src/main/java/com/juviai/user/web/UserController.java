package com.juviai.user.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.juviai.common.dto.UserDTO;
import com.juviai.user.facade.UserFacade;
import com.juviai.user.organisation.converter.AddressConverter;
import com.juviai.user.security.CurrentUser;
import com.juviai.user.security.RequiresBusinessOrHrAdmin;
import com.juviai.user.service.UserAddressService;
import com.juviai.user.service.UserService;
import com.juviai.user.web.dto.IdsRequest;
import com.juviai.user.web.dto.InviteEmployeeRequest;
import com.juviai.user.web.dto.LoginRequest;
import com.juviai.user.web.dto.MeResponseDTO;
import com.juviai.user.web.dto.MyBusinessResponseDTO;
import com.juviai.user.web.dto.OtpRequest;
import com.juviai.user.web.dto.OtpVerificationRequest;
import com.juviai.user.web.dto.CreateUserAddressRequest;
import com.juviai.user.web.dto.SetupPasswordRequest;
import com.juviai.user.web.dto.SignupRequest;
import com.juviai.user.web.dto.UserAddressResponseDTO;
import com.juviai.user.web.dto.UpdateStatusRequest;
import com.juviai.user.web.dto.ExperienceDetailsResponseDTO;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserFacade userFacade;
    private final CurrentUser currentUser;
    private final UserService userService;
    private final UserAddressService userAddressService;
    private final AddressConverter addressConverter;

    /**
     * Send OTP to the provided email address
     * @param request OTP request containing the email
     * @return Success message
     */
    @PostMapping("/otp/send")
    @ResponseStatus(HttpStatus.GONE)
    public Map<String, String> sendOtp(@RequestBody @Valid OtpRequest request) {
        return Map.of(
                "status", "gone",
                "message", "OTP sending is part of signup. Use /api/users/signup via auth-service /auth/register."
        );
    }

    /**
     * Verify the provided OTP for the given email
     * @param request Verification request containing email and OTP
     * @return Success message if OTP is valid
     */
    @PostMapping("/otp/verify")
    @ResponseStatus(HttpStatus.GONE)
    public Map<String, String> verifyOtp(@RequestBody @Valid OtpVerificationRequest request) {
        return Map.of(
                "status", "gone",
                "message", "OTP verification is handled by auth-service. Use /api/auth/verify-otp."
        );
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public MeResponseDTO me(Authentication auth) {
        return userFacade.me(auth);
    }

    @GetMapping("/me/business")
    @PreAuthorize("isAuthenticated()")
    public MyBusinessResponseDTO myBusiness(Authentication auth) {
        return userFacade.myBusiness(auth);
    }

    @GetMapping("/me/experience-details")
    @PreAuthorize("isAuthenticated()")
    public List<ExperienceDetailsResponseDTO> myExperienceDetails(Authentication auth) {
        return userFacade.myExperienceDetails(auth);
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO signup(@RequestBody SignupRequest req) {
        return userFacade.signup(req);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req) {
        return userFacade.login(req);
    }

    @PostMapping("/internal/tokenVersion/increment/{userId}")
    public Map<String, Object> incrementTokenVersion(@PathVariable("userId") UUID userId) {
        return userFacade.incrementTokenVersion(userId);
    }

    @GetMapping("/internal/auth/{userId}")
    public Map<String, Object> authInfo(@PathVariable("userId") UUID userId) {
        return userFacade.authInfo(userId);
    }

    @PostMapping("/internal/status/{userId}")
    public Map<String, Object> updateStatus(@PathVariable("userId") UUID userId,
                                          @RequestBody @Valid UpdateStatusRequest req) {
        return userFacade.updateStatus(userId, req);
    }

    // Internal endpoint for cross-service user lookup by email
    @GetMapping("/internal/byEmail/{email}")
    public MyBusinessResponseDTO getByEmail(@PathVariable("email") String email) {
        return userFacade.getByEmail(email);
    }

    // Business admin invites an employee and assigns roles
    @PostMapping("/{b2bUnitId}/employees/invite")
    @RequiresBusinessOrHrAdmin
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO inviteEmployee(@PathVariable("b2bUnitId") UUID b2bUnitId,
                                                  @RequestBody InviteEmployeeRequest req) {
        return userFacade.inviteEmployee(b2bUnitId, req);
    }

    // Employee sets password first time using setup token
    @PostMapping("/password/setup")
    public Map<String, Object> setupPassword(@RequestBody SetupPasswordRequest req) {
        return userFacade.setupPassword(req);
    }

    // Bulk lookup: given a list of user IDs, return basic user details (for project-service member mapping)
    @PostMapping("/internal/byIds")
    @PreAuthorize("isAuthenticated()")
    public List<UserDTO> getUsersByIds(@RequestBody IdsRequest req) {
        return userFacade.getUsersByIds(req);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public UserDTO getUserById(@PathVariable("userId") @NonNull UUID userId) throws Exception {
        return userFacade.getUserById(userId);
    }

    @PostMapping("/me/addresses")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public UserAddressResponseDTO createMyAddress(@RequestBody @Valid CreateUserAddressRequest request) {
        UUID userId = UUID.fromString(currentUser.id());
        var user = userService.getUserById(userId);
        var saved = userAddressService.createForUser(userId, request);

        UserAddressResponseDTO resp = new UserAddressResponseDTO();
        resp.setUserId(user.getId());
        resp.setUserName((user.getFirstName() != null ? user.getFirstName() : "") +
                (user.getLastName() != null ? (" " + user.getLastName()) : ""));
        resp.setAddress(addressConverter.convert(saved));
        return resp;
    }

    @GetMapping("/me/addresses")
    @PreAuthorize("isAuthenticated()")
    public org.springframework.data.domain.Page<UserAddressResponseDTO> listMyAddresses(Pageable pageable) {
        UUID userId = UUID.fromString(currentUser.id());
        var user = userService.getUserById(userId);
        String userName = (user.getFirstName() != null ? user.getFirstName() : "") +
                (user.getLastName() != null ? (" " + user.getLastName()) : "");

        return userAddressService.listForUser(userId, pageable)
                .map(addr -> {
                    UserAddressResponseDTO r = new UserAddressResponseDTO();
                    r.setUserId(user.getId());
                    r.setUserName(userName);
                    r.setAddress(addressConverter.convert(addr));
                    return r;
                });
    }
}
