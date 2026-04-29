package com.juviai.user.facade.impl;

import com.juviai.common.dto.UserDTO;
import com.juviai.user.converter.AuthInfoResponseConverter;
import com.juviai.user.converter.ExperienceDetailsResponseConverter;
import com.juviai.user.converter.LoginResponseConverter;
import com.juviai.user.converter.MeResponseConverter;
import com.juviai.user.converter.MyBusinessResponseConverter;
import com.juviai.user.converter.UserDtoConverter;
import com.juviai.user.domain.Employee;
import com.juviai.user.domain.User;
import com.juviai.user.facade.UserFacade;
import com.juviai.user.service.OtpService;
import com.juviai.user.service.UserService;
import com.juviai.user.validation.UserValidator;
import com.juviai.user.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserFacadeImpl implements UserFacade {

    private final UserService userService;
    private final OtpService otpService;
    private final UserValidator userValidator;
    private final MeResponseConverter meResponseConverter;
    private final MyBusinessResponseConverter myBusinessResponseConverter;
    private final UserDtoConverter userDtoConverter;
    private final LoginResponseConverter loginResponseConverter;
    private final AuthInfoResponseConverter authInfoResponseConverter;
    private final ExperienceDetailsResponseConverter experienceDetailsResponseConverter;

    @Override
    public Map<String, String> sendOtp(OtpRequest request) {
        try {
            otpService.sendOtp(request.email());
            return Map.of(
                    "status", "success",
                    "message", "OTP has been sent to your email"
            );
        } catch (Exception e) {
            log.error("Error sending OTP: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP. Please try again later.", e);
        }
    }

    @Override
    public Map<String, String> verifyOtp(OtpVerificationRequest request) {
        try {
            boolean isValid = otpService.verifyOtp(request.email(), request.otp());
            if (isValid) {
                return Map.of(
                        "status", "success",
                        "message", "OTP verified successfully"
                );
            } else {
                throw new IllegalArgumentException("Invalid or expired OTP");
            }
        } catch (Exception e) {
            log.error("Error verifying OTP: {}", e.getMessage(), e);
            throw new RuntimeException("Error verifying OTP. Please try again.", e);
        }
    }

    @Override
    public MeResponseDTO me(Authentication auth) {
        String email = resolveEmail(auth);
        log.debug("/me resolved email='{}' authName='{}' authType='{}'", email, auth != null ? auth.getName() : null, auth != null ? auth.getClass().getSimpleName() : null);
        return userService.findByEmail(email)
                .map(meResponseConverter::convert)
                .orElseThrow(() -> {
                    log.warn("/me user not found for email='{}' authName='{}'", email, auth != null ? auth.getName() : null);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
    }

    @Override
    public MyBusinessResponseDTO myBusiness(Authentication auth) {
        String email = resolveEmail(auth);
        log.debug("/me/business resolved email='{}' authName='{}' authType='{}'", email, auth != null ? auth.getName() : null, auth != null ? auth.getClass().getSimpleName() : null);
        return userService.findByEmail(email)
                .map(myBusinessResponseConverter::convert)
                .orElseThrow(() -> {
                    log.warn("/me/business user not found for email='{}' authName='{}'", email, auth != null ? auth.getName() : null);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
    }

    @Override
    public List<ExperienceDetailsResponseDTO> myExperienceDetails(Authentication auth) {
        String email = resolveEmail(auth);
        log.debug("/me/experience-details resolved email='{}' authName='{}' authType='{}'", email, auth != null ? auth.getName() : null, auth != null ? auth.getClass().getSimpleName() : null);
        return experienceDetailsResponseConverter.convertAll(userService.myExperienceDetails(email));
    }

    private static String resolveEmail(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authentication");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof OAuth2AuthenticatedPrincipal oAuth2Principal) {
            String username = oAuth2Principal.getAttribute("username");
            if (username != null && !username.isBlank()) {
                return username.trim();
            }
        }

        String name = auth.getName();
        if (name != null && name.contains("@")) {
            return name.trim();
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Authenticated identity does not contain an email (expected principal attribute 'username' from gateway)"
        );
    }

    @Override
    public UserDTO signup(SignupRequest req) {
        userValidator.validateSignup(req.email, req.mobile, req.password);
        User u = userService.signup(req);
        return userDtoConverter.convert(u);
    }

    @Override
    public Map<String, Object> login(LoginRequest req) {
        return userService.authenticate(req.emailOrMobile, req.password)
                .map(loginResponseConverter::convert)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
    }

    @Override
    public Map<String, Object> incrementTokenVersion(UUID userId) {
        userService.incrementTokenVersion(userId);
        return Map.of("status", "ok");
    }

    @Override
    public Map<String, Object> authInfo(UUID userId) {
        return userService.getById(userId)
                .map(authInfoResponseConverter::convert)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    public Map<String, Object> updateStatus(UUID userId, UpdateStatusRequest req) {
        userService.updateStatus(userId, req.status);
        return Map.of("status", "ok");
    }

    @Override
    public MyBusinessResponseDTO getByEmail(String email) {
        return userService.findByEmail(email)
                .map(myBusinessResponseConverter::convert)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    public UserDTO inviteEmployee(UUID b2bUnitId, InviteEmployeeRequest req) {
        userValidator.validateInviteEmployee(req.getEmail());
        Employee e = userService.inviteEmployee(b2bUnitId, req);
        return userDtoConverter.convert(e);
    }

    @Override
    public Map<String, Object> setupPassword(SetupPasswordRequest req) {
        try {
            userValidator.validatePasswordSetup(req.token, req.newPassword);
        } catch (IllegalArgumentException ex) {
            throw ex;
        }
        boolean ok = userService.setupPassword(req.token, req.newPassword);
        if (!ok) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        return Map.of("status", "ok");
    }

    @Override
    public List<UserDTO> getUsersByIds(IdsRequest req) {
        return userService.getUsersByIds(req.ids).stream()
                .map(userDtoConverter::convert)
                .toList();
    }

    @Override
    public UserDTO getUserById(UUID userId) throws Exception {
        return userDtoConverter.convert(userService.getUserById(userId));
    }
}
