package com.flightbookingapp.repository;

import com.flightbookingapp.model.Role;
import com.flightbookingapp.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Data Tests")
class UserRepositoryTest {

    @Autowired private UserRepository userRepository;

    @Test
    @DisplayName("findByEmailIgnoreCase() finds user by lowercase email")
    void findByEmail_caseInsensitive() {
        userRepository.save(buildUser("Alice@EXAMPLE.COM"));

        Optional<User> result = userRepository.findByEmailIgnoreCase("alice@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("existsByEmailIgnoreCase() returns true for existing email")
    void existsByEmail_returnsTrue() {
        userRepository.save(buildUser("bob@example.com"));
        assertThat(userRepository.existsByEmailIgnoreCase("BOB@EXAMPLE.COM")).isTrue();
    }

    @Test
    @DisplayName("existsByEmailIgnoreCase() returns false for non-existing email")
    void existsByEmail_returnsFalse() {
        assertThat(userRepository.existsByEmailIgnoreCase("nobody@example.com")).isFalse();
    }

    @Test
    @DisplayName("countByRole() counts only users with the given role")
    void countByRole() {
        userRepository.save(buildUser("admin@example.com", Role.ADMIN));
        userRepository.save(buildUser("cust1@example.com", Role.CUSTOMER));
        userRepository.save(buildUser("cust2@example.com", Role.CUSTOMER));

        assertThat(userRepository.countByRole(Role.ADMIN)).isEqualTo(1);
        assertThat(userRepository.countByRole(Role.CUSTOMER)).isEqualTo(2);
    }

    @Test
    @DisplayName("findByVerificationToken() retrieves user by token")
    void findByVerificationToken() {
        User user = buildUser("verify@example.com");
        user.setVerificationToken("token-abc");
        userRepository.save(user);

        assertThat(userRepository.findByVerificationToken("token-abc")).isPresent();
        assertThat(userRepository.findByVerificationToken("wrong-token")).isEmpty();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private User buildUser(String email) {
        return buildUser(email, Role.CUSTOMER);
    }

    private User buildUser(String email, Role role) {
        return User.builder()
                .firstName("Test").lastName("User")
                .email(email.toLowerCase())
                .password("encoded-password")
                .role(role)
                .build();
    }
}
