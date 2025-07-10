package com.skillvault.backend.Controllers;

import com.skillvault.backend.Domain.Enums.SkillStatus;
import com.skillvault.backend.Domain.Skill;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Services.AzureService;
import com.skillvault.backend.Services.TokenService;
import com.skillvault.backend.Services.UserService;
import com.skillvault.backend.dtos.Requests.UpdateUserDTO;
import com.skillvault.backend.dtos.Responses.CertificateResponseDTO;
import com.skillvault.backend.dtos.Responses.SkillResponseDTO;
import com.skillvault.backend.dtos.Responses.UserResponseDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;

import static com.skillvault.backend.Utils.FileUtils.validateProfileImageExtension;

@Slf4j
@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {
    private final AzureService azureService;
    private final TokenService tokenService;
    private final UserService userService;

    @PostMapping("/uploadProfileImg")
    public ResponseEntity<Void> uploadProfileImage(@RequestParam("file") MultipartFile file) {

        if (!validateProfileImageExtension(file)){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "\"file\" form-data parameter must be an Image (webp, jpg, jpeg, png)");
        }
        User user = tokenService.getLoggedEntity();
        azureService.uploadProfilePicture(user, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<UserResponseDTO> getCurrentUser(){
        User user = tokenService.getLoggedEntity();
        return ResponseEntity.ok(new UserResponseDTO(user));
    }

    @GetMapping("/skills")
    public ResponseEntity<List<SkillResponseDTO>> getValidatedSkills(@RequestParam(required = false) Boolean validated){
        User user = tokenService.getLoggedEntity();

        List<Skill> skills = user.getSkills();

        if (validated != null && validated) {
            skills = skills.stream()
                    .filter(skill -> skill.getStatus().equals(SkillStatus.APPROVED))
                    .toList();
        }

        List<SkillResponseDTO> response = skills.stream()
                .map(SkillResponseDTO::new)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/certificates")
    public ResponseEntity<List<CertificateResponseDTO>> getUserCertificates(){
        User user = tokenService.getLoggedEntity();
        List<CertificateResponseDTO> certificates = user.getCertificates()
                .stream().map(CertificateResponseDTO::new).toList();

        return ResponseEntity.ok(certificates);
    }

    @PatchMapping
    public ResponseEntity<UserResponseDTO> updateUser(@RequestBody @Valid UpdateUserDTO data){
        User updatedUser = userService.updateUser(data);
        return ResponseEntity.ok(new UserResponseDTO(updatedUser));
    }
}
