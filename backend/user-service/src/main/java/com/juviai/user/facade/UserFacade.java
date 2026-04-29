package com.juviai.user.facade;

import com.juviai.common.dto.UserDTO;
import com.juviai.user.web.dto.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.juviai.user.web.dto.UpdateStatusRequest;

public interface UserFacade {

    Map<String, String> sendOtp(OtpRequest request);

    Map<String, String> verifyOtp(OtpVerificationRequest request);

    MeResponseDTO me(Authentication auth);

    MyBusinessResponseDTO myBusiness(Authentication auth);

    List<ExperienceDetailsResponseDTO> myExperienceDetails(Authentication auth);

    UserDTO signup(SignupRequest req);

    Map<String, Object> login(LoginRequest req);

    Map<String, Object> incrementTokenVersion(UUID userId);

    Map<String, Object> authInfo(UUID userId);

    Map<String, Object> updateStatus(UUID userId, UpdateStatusRequest req);

    MyBusinessResponseDTO getByEmail(String email);

    UserDTO inviteEmployee(UUID b2bUnitId, InviteEmployeeRequest req);

    Map<String, Object> setupPassword(SetupPasswordRequest req);

    List<UserDTO> getUsersByIds(IdsRequest req);

    UserDTO getUserById(UUID userId) throws Exception;
}
