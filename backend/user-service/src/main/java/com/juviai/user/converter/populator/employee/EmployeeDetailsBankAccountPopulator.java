package com.juviai.user.converter.populator.employee;

import com.juviai.user.converter.populator.EmployeeDetailsPopulator;
import com.juviai.user.crypto.KeyVaultEnvelopeEncryptionService;
import com.juviai.user.domain.BankAccount;
import com.juviai.user.domain.Employee;
import com.juviai.user.dto.EmployeeDetailsDto;
import com.juviai.user.repo.BankAccountRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeDetailsBankAccountPopulator implements EmployeeDetailsPopulator {

    private final BankAccountRepository bankAccountRepository;
    private final KeyVaultEnvelopeEncryptionService keyVaultEnvelopeEncryptionService;

    @Override
    public void populate(Employee source, EmployeeDetailsDto target) {
        Optional<BankAccount> baOpt = bankAccountRepository.findByUserId(source.getId());
        if (baOpt.isEmpty()) {
            target.setAccountNumber(null);
            target.setIfscCode(null);
            return;
        }

        BankAccount ba = baOpt.get();
        target.setAccountNumber(keyVaultEnvelopeEncryptionService.decryptFromPayload(ba.getAccountNumber()));
        target.setIfscCode(keyVaultEnvelopeEncryptionService.decryptFromPayload(ba.getIfscCode()));
    }
}
