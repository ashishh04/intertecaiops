package com.juviai.leave.service.impl;

import com.juviai.leave.domain.*;
import com.juviai.leave.repo.*;
import com.juviai.leave.service.LeaveBalanceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final LeaveBalanceRepository  balanceRepository;
    private final LeaveTypeRepository     leaveTypeRepository;
    private final LeavePolicyRepository   policyRepository;

    @Override
    @Transactional
    public List<LeaveBalance> initializeForEmployee(UUID employeeId, UUID b2bUnitId, int year) {
        LocalDate asOf = LocalDate.of(year, 1, 1);
        List<LeavePolicy> policies = policyRepository.findAllActivePoliciesForOrg(b2bUnitId, asOf);

        List<LeaveBalance> balances = new ArrayList<>();
        for (LeavePolicy policy : policies) {
            if (policy.getAccrualType() == AccrualType.UPFRONT) {
                boolean exists = balanceRepository
                        .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, policy.getLeaveType().getId(), year)
                        .isPresent();
                if (!exists) {
                    LeaveBalance balance = new LeaveBalance(
                            employeeId, policy.getLeaveType(), year, policy.getDaysPerYear());
                    balances.add(balanceRepository.save(balance));
                    log.info("Initialized {} days of {} for employee {} in {}",
                            policy.getDaysPerYear(), policy.getLeaveType().getCode(), employeeId, year);
                }
            }
        }
        return balances;
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveBalance getBalance(UUID employeeId, UUID leaveTypeId, int year) {
        return balanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Leave balance not found for employee " + employeeId
                        + ", type " + leaveTypeId + ", year " + year));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalance> listBalances(UUID employeeId, int year) {
        return balanceRepository.findByEmployeeIdAndYear(employeeId, year);
    }

    @Override
    @Transactional
    public LeaveBalance credit(UUID employeeId, UUID leaveTypeId, int year, BigDecimal days) {
        LeaveBalance balance = getOrCreate(employeeId, leaveTypeId, year);
        balance.setAllocatedDays(balance.getAllocatedDays().add(days));
        log.info("Credited {} days to employee {} leaveType {} year {}", days, employeeId, leaveTypeId, year);
        return balanceRepository.save(balance);
    }

    @Override
    @Transactional
    public LeaveBalance reserve(UUID employeeId, UUID leaveTypeId, int year, BigDecimal days) {
        LeaveBalance balance = getOrCreate(employeeId, leaveTypeId, year);
        if (balance.getAvailableDays().compareTo(days) < 0) {
            // Check if leave type is paid; if not, LOP is allowed
            boolean isPaid = balance.getLeaveType().isPaid();
            if (isPaid) {
                throw new IllegalStateException(
                        "Insufficient leave balance. Available: " + balance.getAvailableDays()
                        + ", Requested: " + days);
            }
            // For paid leaves with zero balance, the excess is treated as LOP
        }
        balance.setPendingDays(balance.getPendingDays().add(days));
        return balanceRepository.save(balance);
    }

    @Override
    @Transactional
    public LeaveBalance release(UUID employeeId, UUID leaveTypeId, int year, BigDecimal days) {
        LeaveBalance balance = getOrCreate(employeeId, leaveTypeId, year);
        BigDecimal newPending = balance.getPendingDays().subtract(days).max(BigDecimal.ZERO);
        balance.setPendingDays(newPending);
        return balanceRepository.save(balance);
    }

    @Override
    @Transactional
    public LeaveBalance consume(UUID employeeId, UUID leaveTypeId, int year, BigDecimal days) {
        LeaveBalance balance = getOrCreate(employeeId, leaveTypeId, year);
        // Release from pending and add to used
        BigDecimal newPending = balance.getPendingDays().subtract(days).max(BigDecimal.ZERO);
        balance.setPendingDays(newPending);
        balance.setUsedDays(balance.getUsedDays().add(days));
        return balanceRepository.save(balance);
    }

    @Override
    @Transactional
    public LeaveBalance addLop(UUID employeeId, UUID leaveTypeId, int year, BigDecimal lopDays) {
        LeaveBalance balance = getOrCreate(employeeId, leaveTypeId, year);
        balance.setLopDays(balance.getLopDays().add(lopDays));
        return balanceRepository.save(balance);
    }

    @Override
    @Transactional
    public List<LeaveBalance> carryForward(UUID b2bUnitId, int fromYear) {
        int toYear = fromYear + 1;
        List<LeaveType> leaveTypes = leaveTypeRepository.findByB2bUnitIdAndActiveTrueOrderByName(b2bUnitId);
        List<LeaveBalance> created = new ArrayList<>();

        for (LeaveType lt : leaveTypes) {
            if (!lt.isCarryForwardAllowed()) continue;

            List<LeaveBalance> yearBalances = balanceRepository.findByEmployeeIdAndYear(null, fromYear)
                    .stream()
                    .filter(b -> b.getLeaveType().getId().equals(lt.getId()))
                    .toList();

            for (LeaveBalance old : yearBalances) {
                BigDecimal unused = old.getAllocatedDays()
                        .add(old.getCarriedForward())
                        .subtract(old.getUsedDays())
                        .max(BigDecimal.ZERO);
                BigDecimal carryAmt = unused.min(BigDecimal.valueOf(lt.getMaxCarryForwardDays()));

                if (carryAmt.compareTo(BigDecimal.ZERO) > 0) {
                    LeaveBalance next = getOrCreate(old.getEmployeeId(), lt.getId(), toYear);
                    next.setCarriedForward(next.getCarriedForward().add(carryAmt));
                    created.add(balanceRepository.save(next));
                    log.info("Carried forward {} days of {} for employee {} → {}",
                            carryAmt, lt.getCode(), old.getEmployeeId(), toYear);
                }
            }
        }
        return created;
    }

    @Override
    @Transactional
    public void accrueMonthly(UUID b2bUnitId, int year, int month) {
        LocalDate asOf = LocalDate.of(year, month, 1);
        List<LeavePolicy> policies = policyRepository.findAllActivePoliciesForOrg(b2bUnitId, asOf)
                .stream()
                .filter(p -> p.getAccrualType() == AccrualType.MONTHLY)
                .toList();

        for (LeavePolicy policy : policies) {
            BigDecimal monthlyCredit = policy.getDaysPerYear()
                    .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            List<LeaveBalance> balances = balanceRepository.findByEmployeeIdAndYear(null, year)
                    .stream()
                    .filter(b -> b.getLeaveType().getId().equals(policy.getLeaveType().getId()))
                    .toList();
            for (LeaveBalance b : balances) {
                b.setAllocatedDays(b.getAllocatedDays().add(monthlyCredit));
                balanceRepository.save(b);
            }
            log.info("Monthly accrual: credited {} days of {} for {}/{}", monthlyCredit, policy.getLeaveType().getCode(), month, year);
        }
    }

    private LeaveBalance getOrCreate(UUID employeeId, UUID leaveTypeId, int year) {
        return balanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .orElseGet(() -> {
                    LeaveType lt = leaveTypeRepository.findById(leaveTypeId)
                            .orElseThrow(() -> new EntityNotFoundException("Leave type not found: " + leaveTypeId));
                    return new LeaveBalance(employeeId, lt, year, BigDecimal.ZERO);
                });
    }
}
