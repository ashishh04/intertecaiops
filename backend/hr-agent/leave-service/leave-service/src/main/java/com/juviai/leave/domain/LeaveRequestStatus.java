package com.juviai.leave.domain;

public enum LeaveRequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED,
    REVOKED     // approved but revoked by HR (e.g. emergency recall)
}
