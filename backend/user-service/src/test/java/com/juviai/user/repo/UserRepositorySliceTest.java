package com.juviai.user.repo;

import com.juviai.common.crypto.AesFieldEncryptor;
import com.juviai.user.domain.Role;
import com.juviai.user.domain.User;
import org.springframework.boot.CommandLineRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.flyway.enabled=false"
})
@Import(AesFieldEncryptor.class)
class UserRepositorySliceTest {

    @MockBean(name = "initData")
    private CommandLineRunner initData;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByEmailIgnoreCase_returnsMatch() {
        User user = buildUser("john", "John.Doe@Example.com", "9990001111", "John", "Doe");
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByEmailIgnoreCase("john.doe@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(user.getId());
    }

    @Test
    void existsByEmailIgnoreCase_isTrueWhenPresent() {
        entityManager.persistAndFlush(buildUser("alice", "ALICE@EXAMPLE.COM", null, "Alice", "L"));

        assertThat(userRepository.existsByEmailIgnoreCase("alice@example.com")).isTrue();
        assertThat(userRepository.existsByEmailIgnoreCase("missing@example.com")).isFalse();
    }

    @Test
    void findByMobile_and_existsByMobile_work() {
        entityManager.persistAndFlush(buildUser("bob", "bob@example.com", "12345", "Bob", "B"));

        assertThat(userRepository.findByMobile("12345")).isPresent();
        assertThat(userRepository.existsByMobile("12345")).isTrue();
        assertThat(userRepository.existsByMobile("54321")).isFalse();
    }

    @Test
    void findByUsername_returnsMatch() {
        User user = buildUser("caseUser", "caseuser@example.com", null, "Case", "User");
        entityManager.persistAndFlush(user);

        assertThat(userRepository.findByUsername(user.getUsername())).isPresent();
    }

    @Test
    void findByPasswordSetupToken_returnsMatch() {
        User user = buildUser("tokenuser", "tokenuser@example.com", null, "Token", "User");
        user.setPasswordSetupToken("setup-token-1");
        entityManager.persistAndFlush(user);

        assertThat(userRepository.findByPasswordSetupToken("setup-token-1")).isPresent();
        assertThat(userRepository.findByPasswordSetupToken("missing")).isNotPresent();
    }

    @Test
    void search_filtersByRoleAndB2bUnitId_andQMatchesAcrossMultipleFields() {
        UUID b2bUnitId = UUID.randomUUID();

        Role adminRole = new Role("ADMIN", "admin", null);
        entityManager.persist(adminRole);

        Role businessRole = new Role("EMPLOYEE", "employee", b2bUnitId);
        entityManager.persist(businessRole);

        User admin = buildUser("admin1", "admin1@example.com", "9001", "System", "Admin");
        admin.addRole(adminRole);
        entityManager.persist(admin);

        User employee = buildUser("jane", "jane@example.com", "9002", "Jane", "Smith");
        employee.addRole(businessRole);
        entityManager.persist(employee);

        User someoneElse = buildUser("x", "x@example.com", "9003", "Xavier", "Other");
        someoneElse.addRole(adminRole);
        entityManager.persistAndFlush(someoneElse);

        Page<User> byRole = userRepository.search(null, null, "ADMIN", PageRequest.of(0, 10));
        assertThat(byRole.getContent()).extracting(User::getUsername).containsExactlyInAnyOrder(admin.getUsername(), someoneElse.getUsername());

        Page<User> byBusinessRole = userRepository.search(b2bUnitId, null, "EMPLOYEE", PageRequest.of(0, 10));
        assertThat(byBusinessRole.getContent()).extracting(User::getUsername).containsExactly(employee.getUsername());

        Page<User> qByFirstName = userRepository.search(null, "jane", null, PageRequest.of(0, 10));
        assertThat(qByFirstName.getContent()).extracting(User::getUsername).contains(employee.getUsername());

        Page<User> qByEmail = userRepository.search(null, "admin1", null, PageRequest.of(0, 10));
        assertThat(qByEmail.getContent()).extracting(User::getUsername).contains(admin.getUsername());
    }

    private static User buildUser(String username, String email, String mobile, String firstName, String lastName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setMobile(mobile);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPasswordHash("hash");
        user.setActive(true);
        return user;
    }
}
