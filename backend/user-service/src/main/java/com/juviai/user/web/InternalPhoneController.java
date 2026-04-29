package com.juviai.user.web;

import com.juviai.user.domain.Role;
import com.juviai.user.domain.User;
import com.juviai.user.domain.UserStatus;
import com.juviai.user.repo.RoleRepository;
import com.juviai.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/internal")
@RequiredArgsConstructor
public class InternalPhoneController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/phone/{phone}")
    public Map<String, Object> findByPhone(@PathVariable("phone") String phone) {
        User user = userRepository.findByMobile(phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for phone: " + phone));
        return toAuthMap(user);
    }

    @PostMapping("/phone-register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> registerFromPhone(@RequestBody Map<String, Object> body) {
        String mobile = Optional.ofNullable(body.get("mobile")).map(String::valueOf).orElse("").trim();
        if (mobile.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mobile is required");
        }

        User existing = userRepository.findByMobile(mobile).orElse(null);
        if (existing != null) {
            if (body.containsKey("active")) {
                existing.setActive(Boolean.parseBoolean(String.valueOf(body.get("active"))));
            }
            if (body.containsKey("status")) {
                try {
                    existing.setStatus(UserStatus.valueOf(String.valueOf(body.get("status"))));
                } catch (Exception ignored) {
                }
            }
            return toAuthMap(userRepository.save(existing));
        }

        String fullName = Optional.ofNullable(body.get("fullName")).map(String::valueOf).orElse("").trim();
        String[] nameParts = fullName.isBlank() ? new String[0] : fullName.split("\\s+", 2);
        String firstName = (nameParts.length >= 1 && !nameParts[0].isBlank()) ? nameParts[0] : "User";
        String lastName = (nameParts.length == 2 && !nameParts[1].isBlank()) ? nameParts[1] : "";

        String email = mobile + "@phone.skillrat.local";
        String username = email;

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Required role ROLE_USER is not initialized"));

        User u = new User();
        u.setMobile(mobile);
        u.setEmail(email.toLowerCase());
        u.setUsername(username.toLowerCase());
        u.setFirstName(firstName);
        u.setLastName(lastName.isBlank() ? "" : lastName);

        boolean active = body.containsKey("active") && Boolean.parseBoolean(String.valueOf(body.get("active")));
        u.setActive(active);

        UserStatus status = UserStatus.ACTIVE;
        if (body.containsKey("status")) {
            try {
                status = UserStatus.valueOf(String.valueOf(body.get("status")));
            } catch (Exception ignored) {
            }
        }
        u.setStatus(status);

        u.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        u.setRoles(Set.of(userRole));

        return toAuthMap(userRepository.save(u));
    }

    private static Map<String, Object> toAuthMap(User user) {
        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "mobile", user.getMobile(),
                "tokenVersion", user.getTokenVersion(),
                "roles", user.getRoles() != null ? user.getRoles().stream().map(Role::getName).toList() : java.util.List.of()
        );
    }
}
