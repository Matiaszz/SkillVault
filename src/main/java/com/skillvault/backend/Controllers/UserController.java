package com.skillvault.backend.Controllers;

import com.skillvault.backend.Domain.Enums.SkillStatus;
import com.skillvault.backend.Domain.Skill;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Services.AzureService;
import com.skillvault.backend.Services.TokenService;
import com.skillvault.backend.Services.UserService;
import com.skillvault.backend.dtos.Requests.UpdateUserDTO;
import com.skillvault.backend.dtos.Responses.SkillResponseDTO;
import com.skillvault.backend.dtos.Responses.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Upload profile image for the logged-in user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file type")
    })
    @PostMapping("/uploadProfileImg")
    public ResponseEntity<Void> uploadProfileImage(
            @Parameter(description = "Profile image file (webp, jpg, jpeg, png)")
            @RequestParam("file") MultipartFile file) {

        if (!validateProfileImageExtension(file)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "\"file\" form-data parameter must be an Image (webp, jpg, jpeg, png)");
        }
        User user = tokenService.getLoggedEntity();
        azureService.uploadProfilePicture(user, file);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get information of the currently authenticated user")
    @ApiResponse(responseCode = "200", description = "User information retrieved successfully")
    @GetMapping
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        User user = tokenService.getLoggedEntity();
        return ResponseEntity.ok(new UserResponseDTO(user));
    }

    @Operation(summary = "Get skills of the currently authenticated user, optionally filtered by status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Skills retrieved successfully")
    })
    @GetMapping("/skills")
    public ResponseEntity<List<SkillResponseDTO>> getSkills(
            @Parameter(description = "Filter by approved skills") @RequestParam(required = false) Boolean validated,
            @Parameter(description = "Filter by pending skills") @RequestParam(required = false) Boolean pending,
            @Parameter(description = "Filter by reproved skills") @RequestParam(required = false) Boolean reproved
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

    @Operation(summary = "Update the currently authenticated user information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid update data"),
            @ApiResponse(responseCode = "403", description = "User not authenticated")
    })
    @PatchMapping
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(description = "User data to update")
            @RequestBody @Valid UpdateUserDTO data) {
        User updatedUser = userService.updateUser(data);
        return ResponseEntity.ok(new UserResponseDTO(updatedUser));
    }
}
