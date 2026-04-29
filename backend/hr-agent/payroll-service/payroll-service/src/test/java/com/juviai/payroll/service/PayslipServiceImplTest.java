package com.juviai.payroll.service;

import com.juviai.payroll.domain.*;
import com.juviai.payroll.repo.PayrollPeriodRepository;
import com.juviai.payroll.repo.PayslipRepository;
import com.juviai.payroll.service.impl.PayslipServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayslipServiceImplTest {

    @Mock private PayslipRepository       payslipRepository;
    @Mock private PayrollPeriodRepository periodRepository;
    @Mock private SalaryStructureService  salaryStructureService;
    @Mock private TaxDeclarationService   taxDeclarationService;

    @InjectMocks
    private PayslipServiceImpl service;

    private PayrollPeriod period;
    private SalaryStructure structure;
    private UUID employeeId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();

        period = new PayrollPeriod(UUID.randomUUID(), 2026, 4, UUID.randomUUID());
        setId(period, UUID.randomUUID());
        period.setStatus(PayrollPeriodStatus.PROCESSING);

        structure = new SalaryStructure(employeeId, "Senior Engineer Band A", LocalDate.of(2026, 1, 1));
        setId(structure, UUID.randomUUID());
        structure.setActive(true);

        // Salary structure: BASIC 60000, HRA 40% of BASIC, SA 20% of BASIC, PF 12% of BASIC (deduction)
        addComponent(structure, ComponentType.BASIC,             "Basic Salary",      CalculationType.FIXED,                new BigDecimal("60000"), true,  true,  0);
        addComponent(structure, ComponentType.HRA,               "House Rent Allow",  CalculationType.PERCENTAGE_OF_BASIC,  new BigDecimal("40"),    true,  true,  1);
        addComponent(structure, ComponentType.SPECIAL_ALLOWANCE, "Special Allowance", CalculationType.PERCENTAGE_OF_BASIC,  new BigDecimal("20"),    true,  true,  2);
    }

    @Test
    @DisplayName("Full month, zero LOP — net pay should equal gross minus deductions")
    void generate_fullMonth_zeroLop() {
        // Arrange
        when(periodRepository.findById(period.getId())).thenReturn(Optional.of(period));
        when(payslipRepository.findByPayrollPeriodIdAndEmployeeId(any(), any())).thenReturn(Optional.empty());
        when(salaryStructureService.getActive(employeeId)).thenReturn(structure);
        when(taxDeclarationService.totalApprovedAmount(any(), any())).thenReturn(BigDecimal.ZERO);
        when(payslipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Payslip payslip = service.generate(period.getId(), employeeId, 0);

        // Assert
        // Basic = 60000, HRA = 24000, SA = 12000 → Gross = 96000
        assertThat(payslip.getGrossEarnings()).isEqualByComparingTo(new BigDecimal("96000.00"));
        assertThat(payslip.getNetPay()).isLessThan(payslip.getGrossEarnings());
        assertThat(payslip.getLopDays()).isZero();
        assertThat(payslip.getPaidDays()).isEqualTo(payslip.getWorkingDays());
        assertThat(payslip.getStatus()).isEqualTo(PayslipStatus.DRAFT);

        // PF should be present as deduction: 12% of min(60000, 15000) = 1800
        boolean pfPresent = payslip.getComponents().stream()
                .anyMatch(c -> c.getComponentType() == ComponentType.PF
                        && !c.isEarning()
                        && c.getAmount().compareTo(new BigDecimal("1800.00")) == 0);
        assertThat(pfPresent).as("PF deduction of 1800 must be present").isTrue();

        // Professional Tax present
        boolean ptPresent = payslip.getComponents().stream()
                .anyMatch(c -> c.getComponentType() == ComponentType.PROFESSIONAL_TAX && !c.isEarning());
        assertThat(ptPresent).as("Professional Tax deduction must be present").isTrue();
    }

    @Test
    @DisplayName("5 LOP days — basic and earnings should be prorated")
    void generate_withLop_earningsShouldBeProrated() {
        when(periodRepository.findById(period.getId())).thenReturn(Optional.of(period));
        when(payslipRepository.findByPayrollPeriodIdAndEmployeeId(any(), any())).thenReturn(Optional.empty());
        when(salaryStructureService.getActive(employeeId)).thenReturn(structure);
        when(taxDeclarationService.totalApprovedAmount(any(), any())).thenReturn(BigDecimal.ZERO);
        when(payslipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // April 2026 = 30 working days, 5 LOP → paid = 25
        Payslip payslip = service.generate(period.getId(), employeeId, 5);

        // Basic should be prorated: 60000 * 25/30 = 50000
        BigDecimal expectedBasic = new BigDecimal("60000")
                .multiply(BigDecimal.valueOf(25))
                .divide(BigDecimal.valueOf(30), 2, java.math.RoundingMode.HALF_UP);

        boolean basicMatches = payslip.getComponents().stream()
                .filter(c -> c.getComponentType() == ComponentType.BASIC && c.isEarning())
                .anyMatch(c -> c.getAmount().compareTo(expectedBasic) == 0);
        assertThat(basicMatches).as("Basic should be prorated for 5 LOP days").isTrue();
        assertThat(payslip.getGrossEarnings()).isLessThan(new BigDecimal("96000.00"));
        assertThat(payslip.getLopDays()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot generate payslip for FINALIZED period")
    void generate_finalizedPeriod_throws() {
        period.setStatus(PayrollPeriodStatus.FINALIZED);
        when(periodRepository.findById(period.getId())).thenReturn(Optional.of(period));

        assertThatThrownBy(() -> service.generate(period.getId(), employeeId, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FINALIZED");
    }

    @Test
    @DisplayName("Cannot regenerate an APPROVED payslip")
    void generate_approvedPayslipExists_throws() {
        Payslip existing = new Payslip();
        existing.setStatus(PayslipStatus.APPROVED);

        when(periodRepository.findById(period.getId())).thenReturn(Optional.of(period));
        when(payslipRepository.findByPayrollPeriodIdAndEmployeeId(any(), any()))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.generate(period.getId(), employeeId, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APPROVED");
    }

    @Test
    @DisplayName("Approve transitions DRAFT payslip to APPROVED")
    void approve_draftPayslip_succeeds() {
        Payslip payslip = new Payslip();
        setId(payslip, UUID.randomUUID());
        payslip.setStatus(PayslipStatus.DRAFT);

        when(payslipRepository.findById(payslip.getId())).thenReturn(Optional.of(payslip));
        when(payslipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Payslip approved = service.approve(payslip.getId(), UUID.randomUUID());
        assertThat(approved.getStatus()).isEqualTo(PayslipStatus.APPROVED);
    }

    @Test
    @DisplayName("Approve a non-DRAFT payslip throws IllegalStateException")
    void approve_nonDraftPayslip_throws() {
        Payslip payslip = new Payslip();
        setId(payslip, UUID.randomUUID());
        payslip.setStatus(PayslipStatus.PAID);

        when(payslipRepository.findById(payslip.getId())).thenReturn(Optional.of(payslip));

        assertThatThrownBy(() -> service.approve(payslip.getId(), UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("High earner: annual gross > 15L should have non-zero TDS")
    void generate_highEarner_shouldHaveTds() {
        // Replace structure with high salary: BASIC 150000/month
        SalaryStructure highSalaryStructure = new SalaryStructure(
                employeeId, "VP Band", LocalDate.of(2026, 1, 1));
        setId(highSalaryStructure, UUID.randomUUID());
        highSalaryStructure.setActive(true);
        addComponent(highSalaryStructure, ComponentType.BASIC, "Basic Salary",
                CalculationType.FIXED, new BigDecimal("150000"), true, true, 0);

        when(periodRepository.findById(period.getId())).thenReturn(Optional.of(period));
        when(payslipRepository.findByPayrollPeriodIdAndEmployeeId(any(), any())).thenReturn(Optional.empty());
        when(salaryStructureService.getActive(employeeId)).thenReturn(highSalaryStructure);
        when(taxDeclarationService.totalApprovedAmount(any(), any())).thenReturn(BigDecimal.ZERO);
        when(payslipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Payslip payslip = service.generate(period.getId(), employeeId, 0);

        boolean tdsPresent = payslip.getComponents().stream()
                .anyMatch(c -> c.getComponentType() == ComponentType.TDS
                        && !c.isEarning()
                        && c.getAmount().compareTo(BigDecimal.ZERO) > 0);
        assertThat(tdsPresent).as("TDS should be non-zero for high earners").isTrue();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void addComponent(SalaryStructure structure, ComponentType type, String name,
                               CalculationType calcType, BigDecimal value,
                               boolean taxable, boolean earning, int order) {
        SalaryStructureComponent c = new SalaryStructureComponent();
        c.setSalaryStructure(structure);
        c.setComponentType(type);
        c.setName(name);
        c.setCalculationType(calcType);
        c.setValue(value);
        c.setTaxable(taxable);
        c.setEarning(earning);
        c.setSortOrder(order);
        structure.getComponents().add(c);
    }

    /** Reflectively set BaseEntity id for testing without DB */
    private void setId(Object entity, UUID id) {
        try {
            var field = entity.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            try {
                var field = entity.getClass().getDeclaredField("id");
                field.setAccessible(true);
                field.set(entity, id);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
