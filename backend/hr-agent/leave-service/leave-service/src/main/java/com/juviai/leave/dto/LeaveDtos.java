package com.juviai.leave.dto;

import com.juviai.leave.domain.*;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class LeaveDtos {


// â”€â”€ Leave Type â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Data
public static class CreateLeaveTypeRequestDto {
    @NotNull  private UUID   b2bUnitId;
    @NotBlank private String code;
    @NotBlank private String name;
    private String  description;
    private boolean paid             = true;
    private boolean requiresDocument = false;
    private Integer maxConsecutiveDays;
    private boolean carryForwardAllowed  = false;
    private int     maxCarryForwardDays  = 0;
    private boolean encashable           = false;
}

@Data
public static class LeaveTypeDto {
    private UUID    id;
    private UUID    b2bUnitId;
    private String  code;
    private String  name;
    private String  description;
    private boolean paid;
    private boolean requiresDocument;
    private Integer maxConsecutiveDays;
    private boolean carryForwardAllowed;
    private int     maxCarryForwardDays;
    private boolean encashable;
    private boolean active;
}

// â”€â”€ Leave Policy â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Data
public static class CreateLeavePolicyRequestDto {
    @NotNull  private UUID        leaveTypeId;
    @NotNull  private UUID        b2bUnitId;
    private String                applicableTo  = "ALL";
    private String                gender;
    @NotNull  @DecimalMin("0.5")
    private BigDecimal            daysPerYear;
    @NotNull  private AccrualType accrualType   = AccrualType.UPFRONT;
    private int                   minTenureDays = 0;
    @NotNull  private LocalDate   effectiveFrom;
    private LocalDate             effectiveTo;
}

@Data
public static class LeavePolicyDto {
    private UUID        id;
    private UUID        leaveTypeId;
    private String      leaveTypeCode;
    private UUID        b2bUnitId;
    private String      applicableTo;
    private String      gender;
    private BigDecimal  daysPerYear;
    private AccrualType accrualType;
    private int         minTenureDays;
    private LocalDate   effectiveFrom;
    private LocalDate   effectiveTo;
    private boolean     active;
}

// â”€â”€ Leave Balance â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Data
public static class LeaveBalanceDto {
    private UUID       id;
    private UUID       employeeId;
    private UUID       leaveTypeId;
    private String     leaveTypeCode;
    private String     leaveTypeName;
    private int        year;
    private BigDecimal allocatedDays;
    private BigDecimal usedDays;
    private BigDecimal pendingDays;
    private BigDecimal carriedForward;
    private BigDecimal lopDays;
    private BigDecimal availableDays;   // computed
}

// â”€â”€ Leave Request â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Data
public static class ApplyLeaveRequestDto {
    @NotNull  private UUID      leaveTypeId;
    @NotNull  private LocalDate fromDate;
    @NotNull  private LocalDate toDate;
    private boolean  halfDay       = false;
    private String   halfDayPeriod;         // MORNING | AFTERNOON
    private String   reason;
    private String   documentUrl;
}

@Data
public static class LeaveRequestDayDto {
    private LocalDate  leaveDate;
    private BigDecimal dayFraction;
}

@Data
public static class LeaveRequestDto {
    private UUID               id;
    private UUID               employeeId;
    private UUID               leaveTypeId;
    private String             leaveTypeCode;
    private String             leaveTypeName;
    private LocalDate          fromDate;
    private LocalDate          toDate;
    private BigDecimal         totalDays;
    private boolean            halfDay;
    private String             halfDayPeriod;
    private String             reason;
    private String             documentUrl;
    private LeaveRequestStatus status;
    private String             rejectionReason;
    private List<LeaveRequestDayDto> days;
}

@Data
public static class ApproveLeaveRequestDto {
    // intentionally empty â€” approval is an action
}

@Data
public static class RejectLeaveRequestDto {
    @NotBlank private String reason;
}

@Data
public static class RevokeLeaveRequestDto {
    @NotBlank private String reason;
}

// â”€â”€ Holiday â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Data
public static class CreateHolidayRequestDto {
    @NotNull  private UUID        b2bUnitId;
    @NotNull  private LocalDate   holidayDate;
    @NotBlank private String      name;
    @NotNull  private HolidayType holidayType = HolidayType.PUBLIC;
    private String                region;
}

@Data
public static class HolidayDto {
    private UUID        id;
    private UUID        b2bUnitId;
    private LocalDate   holidayDate;
    private String      name;
    private HolidayType holidayType;
    private String      region;
}

// â”€â”€ Comp Off â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Data
public static class RequestCompOffDto {
    @NotNull  private LocalDate workedDate;
    @NotBlank private String    reason;
    @DecimalMin("0.5") @DecimalMax("1.0")
    private BigDecimal          credits = BigDecimal.ONE;
}

@Data
public static class CompOffDto {
    private UUID          id;
    private UUID          employeeId;
    private LocalDate     workedDate;
    private String        reason;
    private BigDecimal    credits;
    private CompOffStatus status;
    private LocalDate     expiresAt;
}

// â”€â”€ LOP Summary (for payroll integration) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Data
public static class LopSummaryDto {
    private UUID       employeeId;
    private int        year;
    private int        month;
    private BigDecimal lopDays;
}

}
