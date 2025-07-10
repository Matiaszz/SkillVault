package com.skillvault.backend.Controllers;

import com.skillvault.backend.Domain.Skill;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Services.SkillService;
import com.skillvault.backend.Services.TokenService;
import com.skillvault.backend.dtos.Requests.SkillRequestDTO;
import com.skillvault.backend.dtos.Requests.UpdateSkillDTO;
import com.skillvault.backend.dtos.Responses.SkillResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.skillvault.backend.Validations.DTO.DTOValidator.validateSkillRequest;

@RestController
@RequestMapping("/api/skill")
@AllArgsConstructor
public class SkillController {

    private SkillService skillService;
    private TokenService tokenService;

    @Operation(summary = "Insert a new skill for the logged-in user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Skill created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public ResponseEntity<?> insertSkill(@RequestBody @Valid SkillRequestDTO dto) {
        List<String> errors = validateSkillRequest(dto);

        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        User user = tokenService.getLoggedEntity();
        Skill response = skillService.registerSkill(user, dto);
        return ResponseEntity.ok(new SkillResponseDTO(response));
    }

    @Operation(summary = "Get skills of the currently authenticated user")
    @ApiResponse(responseCode = "200", description = "Skills retrieved successfully")
    @GetMapping("/my")
    public ResponseEntity<Page<SkillResponseDTO>> getUserSkills(Pageable pageable) {
        User user = tokenService.getLoggedEntity();
        Page<Skill> skills = skillService.getUserSkills(user.getId(), pageable);
        Page<SkillResponseDTO> response = skills.map(SkillResponseDTO::new);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update a skill by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Skill updated successfully"),
            @ApiResponse(responseCode = "404", description = "Skill not found")
    })
    @PatchMapping("/{skillId}")
    public ResponseEntity<SkillResponseDTO> updateSkill(
            @Parameter(description = "UUID of the skill") @PathVariable UUID skillId,
            @RequestBody UpdateSkillDTO data
    ) {
        Skill updatedSkill = skillService.updateSkill(data, skillId);
        return ResponseEntity.ok(new SkillResponseDTO(updatedSkill));
    }

    @Operation(summary = "Delete a skill by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Skill deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Skill not found")
    })
    @DeleteMapping("/{skillId}")
    public ResponseEntity<Void> deleteSkill(
            @Parameter(description = "UUID of the skill") @PathVariable UUID skillId
    ) {
        skillService.deleteSkillIfExists(skillId);
        return ResponseEntity.noContent().build();
    }
}
