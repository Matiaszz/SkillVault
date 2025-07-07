package com.skillvault.backend.Controllers;

import com.skillvault.backend.Domain.Skill;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Services.SkillService;
import com.skillvault.backend.Services.TokenService;
import com.skillvault.backend.dtos.Requests.SkillRequestDTO;
import com.skillvault.backend.dtos.Requests.UpdateSkillDTO;
import com.skillvault.backend.dtos.Responses.SkillResponseDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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

    @PostMapping
    public ResponseEntity<?> insertSkill(@RequestBody @Valid SkillRequestDTO dto){
        List<String> errors = validateSkillRequest(dto);

        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }
        User user = tokenService.getLoggedEntity();
        SkillResponseDTO response = skillService.registerSkill(user, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<SkillResponseDTO>> getUserSkills(){
        User user = tokenService.getLoggedEntity();
        List<SkillResponseDTO> response = user.getSkills()
                .stream()
                .map(SkillResponseDTO::new)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{skillId}")
    public ResponseEntity<SkillResponseDTO> updateSkill(@PathVariable UUID skillId, @RequestBody UpdateSkillDTO data){
        Skill updatedSkill = skillService.updateSkill(data, skillId);
        return ResponseEntity.ok(new SkillResponseDTO(updatedSkill));
    }

    @DeleteMapping("/{skillId}")
    public ResponseEntity<Void> deleteSkill(@PathVariable UUID skillId) {
        skillService.deleteSkillIfExists(skillId);
        return ResponseEntity.noContent().build();
    }
}