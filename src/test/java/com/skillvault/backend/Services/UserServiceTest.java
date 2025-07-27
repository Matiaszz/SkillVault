package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Repositories.UserRepository;
import com.skillvault.backend.config.TestConfig;
import com.skillvault.backend.dtos.Requests.LoginUserDTO;
import com.skillvault.backend.dtos.Requests.UpdateUserDTO;
import com.skillvault.backend.dtos.Requests.UserRequestDTO;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    private AutoCloseable closeable;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;


    @Autowired
    @InjectMocks
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup(){
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception{
        closeable.close();
    }


    @Test
    @DisplayName("should register user with role USER in db and ensure that's right")
    @Order(1)
    void registerUserSuccess() {
        UserRequestDTO data = registerUser("userUserTest", "useruser@gmail.com", UserRole.USER);
        var foundUser = userRepository.findByEmail(data.email());

        assertThat(foundUser)
                .isPresent()
                .get()
                .extracting(User::getRole)
                .isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("should register user with role EVALUATOR in db and ensure that's right")
    @Order(2)
    void registerEvaluatorSuccess() {
        UserRequestDTO data = registerUser("userEvalTest", "usereval@gmail.com", UserRole.EVALUATOR);
        var foundUser = userRepository.findByEmail(data.email());

        assertThat(foundUser)
                .isPresent()
                .get()
                .extracting(User::getRole)
                .isEqualTo(UserRole.EVALUATOR);
    }

    @Test
    @DisplayName("should register user with role ADMIN in db and ensure that's right")
    @Order(3)
    void registerAdminSuccess() {
        UserRequestDTO data = registerUser("userAdminTest", "userAdmin@gmail.com", UserRole.ADMIN);
        var foundUser = userRepository.findByEmail(data.email());

        assertThat(foundUser)
                .isPresent()
                .get()
                .extracting(User::getRole)
                .isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("Should log-in user successfully")
    @Order(4)
    void loginSuccess(){
        LoginUserDTO login = new LoginUserDTO("userAdmin@gmail.com", "Password@123");
        User loggedUser = userService.loginUser(login);

        assertThat(loggedUser.getId() != null).isTrue();
    }


    @Test
    @DisplayName("Shouldn't log-in user")
    @Order(5)
    void loginFailed(){
        LoginUserDTO login = new LoginUserDTO("userAdmin@gmail.com", "Password@123");
        try {
            userService.loginUser(login);
        } catch (ResponseStatusException ex){
            Assertions.assertNotNull(ex.getReason());
            assertThat(ex.getReason().equals("User not found")).isTrue();
        }
    }

    @Test
    @DisplayName("Should update user successfully")
    @Order(6)
    void updateUser(){
        User user = updateModel();
        Optional<User> updatedUser = userRepository.findByEmail(user.getEmail());
        assertThat(updatedUser.isPresent() && updatedUser.get().getName().equals("Hello")).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when login fails due to invalid user")
    @Order(7)
    void loginShouldFailForInvalidUser() {
        LoginUserDTO login = new LoginUserDTO("invalid@example.com", "Password@123");

        ResponseStatusException thrown = Assertions.assertThrows(
                ResponseStatusException.class,
                () -> userService.loginUser(login)
        );

        assertThat(thrown.getReason()).isEqualTo("User not found");
    }

    private User updateModel(){
        UpdateUserDTO dto = new UpdateUserDTO("Hello", null, null, null, null, null);
        LoginUserDTO login = new LoginUserDTO("userAdmin@gmail.com", "Password@123");
        User user = userService.loginUser(login);
        user.updateFromDTO(dto);
        userRepository.save(user);
        return user;
    }

    private UserRequestDTO registerUser(String username, String email, UserRole role){
        UserRequestDTO data = new UserRequestDTO(
                username, "userTest", email, "Password@123");

        userService.registerUser(data, role);
        return data;
    }
}
