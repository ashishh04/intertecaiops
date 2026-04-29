package com.juviai.user.facade.impl;

import com.juviai.common.dto.UserDTO;
import com.juviai.user.converter.AuthInfoResponseConverter;
import com.juviai.user.converter.LoginResponseConverter;
import com.juviai.user.converter.MeResponseConverter;
import com.juviai.user.converter.MyBusinessResponseConverter;
import com.juviai.user.converter.UserDtoConverter;
import com.juviai.user.domain.Employee;
import com.juviai.user.domain.User;
import com.juviai.user.service.OtpService;
import com.juviai.user.service.UserService;
import com.juviai.user.validation.UserValidator;
import com.juviai.user.web.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFacadeImplTest {

    @Mock
    private UserService userService;

    @Mock
    private OtpService otpService;

    @Mock
    private UserValidator userValidator;

    @Mock
    private MeResponseConverter meResponseConverter;

    @Mock
    private MyBusinessResponseConverter myBusinessResponseConverter;

    @Mock
    private UserDtoConverter userDtoConverter;

    @Mock
    private LoginResponseConverter loginResponseConverter;

    @Mock
    private AuthInfoResponseConverter authInfoResponseConverter;

    @InjectMocks
    private UserFacadeImpl facade;

    @Test
    void sendOtp_returnsOkOnSuccess() {
        Map<String, String> res = facade.sendOtp(new OtpRequest("a@b.com"));

        assertThat(res).containsEntry("status", "success");
        verify(otpService).sendOtp("a@b.com");
    }

    @Test
    void sendOtp_returns500OnException() {
        doThrow(new RuntimeException("boom")).when(otpService).sendOtp("a@b.com");

        assertThatThrownBy(() -> facade.sendOtp(new OtpRequest("a@b.com")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send OTP");
    }

    @Test
    void verifyOtp_returnsOkWhenValid() {
        when(otpService.verifyOtp("a@b.com", "123")).thenReturn(true);

        Map<String, String> res = facade.verifyOtp(new OtpVerificationRequest("a@b.com", "123"));

        assertThat(res).containsEntry("status", "success");
    }

    @Test
    void verifyOtp_returns400WhenInvalid() {
        when(otpService.verifyOtp("a@b.com", "123")).thenReturn(false);

        assertThatThrownBy(() -> facade.verifyOtp(new OtpVerificationRequest("a@b.com", "123")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error verifying OTP");
    }

    @Test
    void me_returns200WhenUserFound() {
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("a@b.com");

        User u = new User();
        MeResponseDTO dto = new MeResponseDTO();

        when(userService.findByEmail("a@b.com")).thenReturn(Optional.of(u));
        when(meResponseConverter.convert(u)).thenReturn(dto);

        MeResponseDTO res = facade.me(auth);

        assertThat(res).isSameAs(dto);
    }

    @Test
    void me_returns404WhenUserMissing() {
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("a@b.com");
        when(userService.findByEmail("a@b.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.me(auth))
                .isInstanceOf(ResponseStatusException.class)
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode().equals(HttpStatus.NOT_FOUND))
                .hasMessageContaining("User not found");
    }

    @Test
    void myBusiness_returns200WhenUserFound() {
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("a@b.com");

        User u = new User();
        MyBusinessResponseDTO dto = new MyBusinessResponseDTO();

        when(userService.findByEmail("a@b.com")).thenReturn(Optional.of(u));
        when(myBusinessResponseConverter.convert(u)).thenReturn(dto);

        MyBusinessResponseDTO res = facade.myBusiness(auth);

        assertThat(res).isSameAs(dto);
    }

    @Test
    void signup_validatesDelegatesConverts_andReturnsOk() {
        SignupRequest req = new SignupRequest();
        req.email = "a@b.com";
        req.mobile = "999";
        req.password = "password123";

        User u = new User();
        UserDTO dto = new UserDTO();

        when(userService.signup(req)).thenReturn(u);
        when(userDtoConverter.convert(u)).thenReturn(dto);

        UserDTO res = facade.signup(req);

        assertThat(res).isSameAs(dto);

        verify(userValidator).validateSignup("a@b.com", "999", "password123");
        verify(userService).signup(req);
    }

    @Test
    void login_returns200WhenAuthenticated() {
        LoginRequest req = new LoginRequest();
        req.emailOrMobile = "a@b.com";
        req.password = "pw";

        User u = new User();
        Map<String, Object> loginRes = Map.of("token", "t");

        when(userService.authenticate("a@b.com", "pw")).thenReturn(Optional.of(u));
        when(loginResponseConverter.convert(u)).thenReturn(loginRes);

        Map<String, Object> res = facade.login(req);

        assertThat(res).isEqualTo(loginRes);
    }

    @Test
    void login_returns401WhenInvalidCredentials() {
        LoginRequest req = new LoginRequest();
        req.emailOrMobile = "a@b.com";
        req.password = "pw";

        when(userService.authenticate("a@b.com", "pw")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.login(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid credentials");
        verify(loginResponseConverter, never()).convert(any());
    }

    @Test
    void incrementTokenVersion_returnsOk() {
        UUID id = UUID.randomUUID();

        Map<String, Object> res = facade.incrementTokenVersion(id);

        assertThat(res).containsEntry("status", "ok");
        verify(userService).incrementTokenVersion(id);
    }

    @Test
    void authInfo_returns200WhenUserFound() {
        UUID id = UUID.randomUUID();
        User u = new User();
        Map<String, Object> authInfo = Map.of("k", "v");

        when(userService.getById(id)).thenReturn(Optional.of(u));
        when(authInfoResponseConverter.convert(u)).thenReturn(authInfo);

        Map<String, Object> res = facade.authInfo(id);

        assertThat(res).isEqualTo(authInfo);
    }

    @Test
    void authInfo_returns404WhenMissing() {
        UUID id = UUID.randomUUID();
        when(userService.getById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.authInfo(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getByEmail_returns200WhenFound() {
        User u = new User();
        MyBusinessResponseDTO dto = new MyBusinessResponseDTO();

        when(userService.findByEmail("a@b.com")).thenReturn(Optional.of(u));
        when(myBusinessResponseConverter.convert(u)).thenReturn(dto);

        MyBusinessResponseDTO res = facade.getByEmail("a@b.com");

        assertThat(res).isSameAs(dto);
    }

    @Test
    void inviteEmployee_validatesDelegatesConverts() {
        UUID b2bUnitId = UUID.randomUUID();
        InviteEmployeeRequest req = new InviteEmployeeRequest();
        req.setEmail("e@b.com");

        Employee e = new Employee();
        UserDTO dto = new UserDTO();

        when(userService.inviteEmployee(b2bUnitId, req)).thenReturn(e);
        when(userDtoConverter.convert(e)).thenReturn(dto);

        UserDTO res = facade.inviteEmployee(b2bUnitId, req);

        assertThat(res).isSameAs(dto);
        verify(userValidator).validateInviteEmployee("e@b.com");
    }

    @Test
    void setupPassword_returns400WhenValidatorThrows() {
        SetupPasswordRequest req = new SetupPasswordRequest();
        req.token = "t";
        req.newPassword = "password123";

        doThrow(new IllegalArgumentException("bad"))
                .when(userValidator).validatePasswordSetup("t", "password123");

        assertThatThrownBy(() -> facade.setupPassword(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bad");
        verify(userService, never()).setupPassword(any(), any());
    }

    @Test
    void setupPassword_returnsOkWhenServiceReturnsTrue() {
        SetupPasswordRequest req = new SetupPasswordRequest();
        req.token = "t";
        req.newPassword = "password123";

        when(userService.setupPassword("t", "password123")).thenReturn(true);

        Map<String, Object> res = facade.setupPassword(req);

        assertThat(res).containsEntry("status", "ok");
    }

    @Test
    void setupPassword_returns400WhenServiceReturnsFalse() {
        SetupPasswordRequest req = new SetupPasswordRequest();
        req.token = "t";
        req.newPassword = "password123";

        when(userService.setupPassword("t", "password123")).thenReturn(false);

        assertThatThrownBy(() -> facade.setupPassword(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid or expired token");
    }

    @Test
    void getUsersByIds_mapsEachUserToDto() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();

        IdsRequest req = new IdsRequest();
        req.ids = List.of(a, b);

        User u1 = new User();
        User u2 = new User();

        UserDTO d1 = new UserDTO();
        UserDTO d2 = new UserDTO();

        when(userService.getUsersByIds(req.ids)).thenReturn(List.of(u1, u2));
        when(userDtoConverter.convert(u1)).thenReturn(d1);
        when(userDtoConverter.convert(u2)).thenReturn(d2);

        List<UserDTO> res = facade.getUsersByIds(req);

        assertThat(res).containsExactly(d1, d2);
    }

    @Test
    void getUserById_delegatesAndConverts() throws Exception {
        UUID id = UUID.randomUUID();
        User u = new User();
        UserDTO dto = new UserDTO();

        when(userService.getUserById(id)).thenReturn(u);
        when(userDtoConverter.convert(u)).thenReturn(dto);

        UserDTO result = facade.getUserById(id);

        assertThat(result).isSameAs(dto);
        verify(userService).getUserById(id);
    }
}
