package com.juviai.user.web.dto;

import com.juviai.user.domain.UserStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {
    @NotNull public UserStatus status;
}
