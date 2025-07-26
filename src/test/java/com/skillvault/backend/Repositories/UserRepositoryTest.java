package com.skillvault.backend.Repositories;

import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Security.SecurityConfig;
import com.skillvault.backend.config.TestConfig;
import com.skillvault.backend.dtos.Requests.UserRequestDTO;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class UserRepositoryTest {
    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private  EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Should get user successfully from database")
    void findByEmailSuccess() {
        UserRequestDTO data = new UserRequestDTO(
                "UsernameTest",
                "UserTest",
                "user@email.com",
                "MyPassword@123");

        User createdUser = createUser(data);
        Optional<User> result = userRepository.findByEmail(createdUser.getEmail());

        assertThat(result.isPresent()).isTrue();

    }

    @Test
    @DisplayName("Shouldn't get user from database if not exists")
    void findByEmailFailure() {

        Optional<User> result = userRepository.findByEmail("user@email.comERROR");

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void findByRole() {
    }

    private User createUser(UserRequestDTO data){
        User newUser = new User(data, UserRole.USER);
        newUser.setPassword(passwordEncoder.encode(data.password()));

        this.entityManager.persist(newUser);
        return newUser;
    }
}