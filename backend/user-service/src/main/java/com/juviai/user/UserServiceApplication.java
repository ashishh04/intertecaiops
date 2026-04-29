package com.juviai.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import com.juviai.user.domain.BankAccount;
import com.juviai.user.domain.Compensation;
import com.juviai.user.domain.Education;
import com.juviai.user.domain.Employee;
import com.juviai.user.domain.ProfileExperience;
import com.juviai.user.domain.Role;
import com.juviai.user.domain.TitleRecord;
import com.juviai.user.domain.User;
import com.juviai.user.domain.UserSkill;
import com.juviai.user.organisation.domain.B2BUnit;
import com.juviai.user.organisation.domain.B2BUnitStatus;
import com.juviai.user.organisation.domain.B2BUnitType;
import com.juviai.user.organisation.repo.B2BUnitRepository;
import com.juviai.user.crypto.KeyVaultEnvelopeEncryptionService;
import com.juviai.common.crypto.AesFieldEncryptor;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.juviai.user.client")
@SpringBootApplication(scanBasePackages = {"com.juviai.user", "com.juviai.common"})
@ConfigurationPropertiesScan
@EnableAsync
@Import(AesFieldEncryptor.class)
@EntityScan(basePackageClasses = {
        User.class, Employee.class, Role.class, ProfileExperience.class,
        Education.class, UserSkill.class, TitleRecord.class, B2BUnit.class, BankAccount.class, Compensation.class
})
@EnableJpaRepositories(basePackages = {
        "com.juviai.user.repo",
        "com.juviai.user.organisation.repo"
})
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    @ConditionalOnProperty(prefix = "skillrat.crypto.keyvault", name = "validate-on-startup", havingValue = "true", matchIfMissing = true)
    public CommandLineRunner validateEncryptionConfiguration(KeyVaultEnvelopeEncryptionService keyVaultEnvelopeEncryptionService) {
        return args -> keyVaultEnvelopeEncryptionService.validateConfiguration();
    }

    @Bean
    public CommandLineRunner initData(B2BUnitRepository b2bUnitRepository) {
        return args -> {
            if (b2bUnitRepository.count() == 0) {
                B2BUnit defaultUnit = new B2BUnit();
                defaultUnit.setName("Default Organization");
                defaultUnit.setType(B2BUnitType.ORGANIZATION);
                defaultUnit.setStatus(B2BUnitStatus.APPROVED);
                b2bUnitRepository.save(defaultUnit);
                System.out.println("Created default B2BUnit with ID: " + defaultUnit.getId());
            }
        };
    }
}
