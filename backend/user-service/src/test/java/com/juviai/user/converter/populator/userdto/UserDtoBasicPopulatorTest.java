package com.juviai.user.converter.populator.userdto;

import com.juviai.common.dto.UserDTO;
import com.juviai.user.domain.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserDtoBasicPopulatorTest {

    private final UserDtoBasicPopulator populator = new UserDtoBasicPopulator();

    @Test
    void populate_copiesBasicFields() {
        UUID id = UUID.randomUUID();

        User source = new User();
        source.setId(id);
        source.setFirstName("John");
        source.setLastName("Doe");
        source.setEmail("john.doe@example.com");

        UserDTO target = new UserDTO();

        populator.populate(source, target);

        assertThat(target.getId()).isEqualTo(id);
        assertThat(target.getFirstName()).isEqualTo("John");
        assertThat(target.getLastName()).isEqualTo("Doe");
        assertThat(target.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void populate_overwritesExistingTargetValues() {
        User source = new User();
        source.setId(UUID.randomUUID());
        source.setFirstName("A");
        source.setLastName("B");
        source.setEmail("a@b.com");

        UserDTO target = new UserDTO();
        target.setFirstName("old");
        target.setLastName("old");
        target.setEmail("old@old.com");

        populator.populate(source, target);

        assertThat(target.getFirstName()).isEqualTo("A");
        assertThat(target.getLastName()).isEqualTo("B");
        assertThat(target.getEmail()).isEqualTo("a@b.com");
    }
}
