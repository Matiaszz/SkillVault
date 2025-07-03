package com.skillvault.backend.Controllers;

import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Services.TokenService;
import com.skillvault.backend.Services.UserService;
import com.skillvault.backend.dtos.Requests.LoginUserDTO;
import com.skillvault.backend.dtos.Requests.UserRequestDTO;
import com.skillvault.backend.dtos.Responses.UserResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final UserService userService;

    private final TokenService tokenService;

    @PostMapping("/user/register")
    public ResponseEntity<UserResponseDTO> userRegister(@RequestBody @Valid UserRequestDTO data){
        return this.register(data, UserRole.USER, true);
    }

    @PostMapping("/evaluator/register")
    public ResponseEntity<UserResponseDTO> evaluatorRegister(@RequestBody @Valid UserRequestDTO data){
        return this.register(data, UserRole.EVALUATOR, false);
    }

    @PostMapping("/admin/register")
    public ResponseEntity<UserResponseDTO> adminRegister(@RequestBody @Valid UserRequestDTO data){
        return this.register(data, UserRole.ADMIN, false);
    }

    @PostMapping("/user/login")
    public ResponseEntity<UserResponseDTO> userLogin(@RequestBody @Valid LoginUserDTO data, HttpServletRequest request){
        if (tokenService.hasToken(request)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An user is already authenticated. Please logout first.");
        }

        User loggedUser = userService.loginUser(data);
        return tokenService.generateUserTokenAndCreateCookie(loggedUser);
    }


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

    private ResponseEntity<UserResponseDTO> register(UserRequestDTO data, UserRole role, boolean autoLogin){
        User user = userService.registerUser(data, role);
        if (autoLogin){
            return tokenService.generateUserTokenAndCreateCookie(user);
        }
        return ResponseEntity.ok(new UserResponseDTO(user));
    }
}
