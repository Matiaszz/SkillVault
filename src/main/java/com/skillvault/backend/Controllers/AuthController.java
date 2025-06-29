package com.skillvault.backend.Controllers;

import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Services.TokenService;
import com.skillvault.backend.Services.UserService;
import com.skillvault.backend.dtos.Requests.UserRequestDTO;
import com.skillvault.backend.dtos.Responses.UserResponseDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final UserService userService;

    private final TokenService tokenService;

    @PostMapping("/user/register/")
    public ResponseEntity<UserResponseDTO> userRegister(@RequestBody @Valid UserRequestDTO data){
        User user = userService.registerUser(data);
        log.info("Salvooooooooooo"); // nao ta chegando
        return tokenService.generateUserTokenAndCreateCookie(user);
    }


}
