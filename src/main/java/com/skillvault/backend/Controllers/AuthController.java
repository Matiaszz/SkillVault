package com.skillvault.backend.Controllers;

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
import org.springframework.http.HttpStatus;
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

    @PostMapping("/user/register/")
    public ResponseEntity<UserResponseDTO> userRegister(@RequestBody @Valid UserRequestDTO data, HttpServletRequest request){
        if (tokenService.hasToken(request)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An user is already authenticated. Please logout first.");
        }

        User user = userService.registerUser(data);
        return tokenService.generateUserTokenAndCreateCookie(user);
    }




}
