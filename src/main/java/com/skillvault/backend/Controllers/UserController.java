package com.skillvault.backend.Controllers;

import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Domain.UserProfilePicture;
import com.skillvault.backend.Services.AzureService;
import com.skillvault.backend.Services.TokenService;
import com.skillvault.backend.Services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {
    private final AzureService azureService;
    private final TokenService tokenService;
    private final UserService userService;

    @PostMapping("/uploadProfileImg")
    public ResponseEntity<Void> uploadProfileImage(@RequestParam MultipartFile file) {
        User user = tokenService.getLoggedEntity();

            azureService.uploadProfilePicture(user, file);
            return ResponseEntity.ok().build();


    }



}
