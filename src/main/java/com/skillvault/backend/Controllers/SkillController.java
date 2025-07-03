package com.skillvault.backend.Controllers;

import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Services.SkillService;
import com.skillvault.backend.Services.TokenService;
import com.skillvault.backend.dtos.Requests.SkillRequestDTO;
import com.skillvault.backend.dtos.Responses.SkillResponseDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
}
