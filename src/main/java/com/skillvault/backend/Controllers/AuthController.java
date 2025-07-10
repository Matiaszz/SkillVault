package com.skillvault.backend.Controllers;

import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Services.TokenService;
import com.skillvault.backend.Services.UserService;
import com.skillvault.backend.dtos.Requests.LoginUserDTO;
import com.skillvault.backend.dtos.Requests.UserRequestDTO;
import com.skillvault.backend.dtos.Responses.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static com.skillvault.backend.Validations.DTO.DTOValidator.validateLogin;
import static com.skillvault.backend.Validations.DTO.DTOValidator.validateUserRequest;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login and logout")
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;

    @Operation(summary = "Register as USER", description = "Registers a new user with USER role and logs in automatically.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered and logged in"),
            @ApiResponse(responseCode = "400", description = "Validation errors"),
            @ApiResponse(responseCode = "409", description = "Email or username already exists")
    })
    @PostMapping("/user/register")
    public ResponseEntity<?> userRegister(@RequestBody @Valid UserRequestDTO data) {
        return this.register(data, UserRole.USER, true);
    }

    @Operation(summary = "Register as EVALUATOR", description = "Registers a new evaluator. Does not auto-login.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evaluator registered"),
            @ApiResponse(responseCode = "400", description = "Validation errors"),
            @ApiResponse(responseCode = "409", description = "Email or username already exists")
    })
    @PostMapping("/evaluator/register")
    public ResponseEntity<?> evaluatorRegister(@RequestBody @Valid UserRequestDTO data) {
        return this.register(data, UserRole.EVALUATOR, false);
    }

    @Operation(summary = "Register as ADMIN", description = "Registers a new admin. Does not auto-login.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Admin registered"),
            @ApiResponse(responseCode = "400", description = "Validation errors"),
            @ApiResponse(responseCode = "409", description = "Email or username already exists")
    })
    @PostMapping("/admin/register")
    public ResponseEntity<?> adminRegister(@RequestBody @Valid UserRequestDTO data) {
        return this.register(data, UserRole.ADMIN, false);
    }

    @Operation(summary = "Login user", description = "Authenticates a user and returns a cookie-based JWT token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation errors"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "409", description = "User already logged in")
    })
    @PostMapping("/user/login")
    public ResponseEntity<?> userLogin(@RequestBody @Valid LoginUserDTO data, HttpServletRequest request) {
        List<String> errors = validateLogin(data);

        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        if (tokenService.hasToken(request)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An user is already authenticated. Please logout first.");
        }

        User loggedUser = userService.loginUser(data);
        return tokenService.generateUserTokenAndCreateCookie(loggedUser);
    }

    @Operation(summary = "Logout", description = "Clears the user authentication cookie.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User logged out successfully")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie expiredCookie = ResponseCookie.from("userToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .build();
    }

    private ResponseEntity<?> register(UserRequestDTO data, UserRole role, boolean autoLogin) {
        List<String> errors = validateUserRequest(data);

        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        User user = userService.registerUser(data, role);
        if (autoLogin) {
            return tokenService.generateUserTokenAndCreateCookie(user);
        }

        return ResponseEntity.ok(new UserResponseDTO(user));
    }
}
