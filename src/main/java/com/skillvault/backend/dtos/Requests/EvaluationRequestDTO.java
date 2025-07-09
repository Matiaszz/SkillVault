package com.skillvault.backend.dtos.Requests;

import com.skillvault.backend.dtos.Responses.SkillResponseDTO;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public record EvaluationRequestDTO(
        @NotBlank String title,
        List<SkillResponseDTO> approvedSkills,
        List<SkillResponseDTO> reprovedSkills,
        String obs
) {
        public EvaluationRequestDTO {
                approvedSkills = approvedSkills != null ? approvedSkills : new ArrayList<>();
                reprovedSkills = reprovedSkills != null ? reprovedSkills : new ArrayList<>();
        }
}
