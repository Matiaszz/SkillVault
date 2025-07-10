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
    public ResponseEntity<List<SkillResponseDTO>> getSkills(
            @RequestParam(required = false) Boolean validated,
            @RequestParam(required = false) Boolean pending,
            @RequestParam(required = false) Boolean reproved
    ) {
        User user = tokenService.getLoggedEntity();

        List<Skill> userSkills = user.getSkills();

        if (validated == null && pending == null && reproved == null) {
            return ResponseEntity.ok(
                    userSkills.stream().map(SkillResponseDTO::new).toList()
            );
        }

        List<Skill> filteredSkills = userSkills.stream()
                .filter(skill ->
                        (Boolean.TRUE.equals(validated) && skill.getStatus().equals(SkillStatus.APPROVED)) ||
                                (Boolean.TRUE.equals(pending) && skill.getStatus().equals(SkillStatus.PENDING)) ||
                                (Boolean.TRUE.equals(reproved) && skill.getStatus().equals(SkillStatus.REPROVED))
                )
                .toList();

        List<SkillResponseDTO> response = filteredSkills.stream()
                .map(SkillResponseDTO::new)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PatchMapping
    public ResponseEntity<UserResponseDTO> updateUser(@RequestBody @Valid UpdateUserDTO data){
        User updatedUser = userService.updateUser(data);
        return ResponseEntity.ok(new UserResponseDTO(updatedUser));
    }
}
