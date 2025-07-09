package com.skillvault.backend.dtos.Responses;

import com.skillvault.backend.Domain.Evaluation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EvaluationResponseDTO(
        UUID id,
        String title,
        UserSimpleDTO evaluator,
        UserSimpleDTO evaluatedUser,
        CertificateResponseDTO certificate,
        List<SkillResponseDTO> approvedSkills,
        List<SkillResponseDTO> reprovedSkills,
        String obs,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public EvaluationResponseDTO(Evaluation evaluation){
        this(
                evaluation.getId(),
                evaluation.getTitle(),
                new UserSimpleDTO(evaluation.getEvaluator()),
                new UserSimpleDTO(evaluation.getEvaluatedUser()),
                new CertificateResponseDTO(evaluation.getCertificate()),
                evaluation.getApprovedSkills().stream().map(SkillResponseDTO::new).toList(),
                evaluation.getReprovedSkills().stream().map(SkillResponseDTO::new).toList(),
                evaluation.getObs(),
                evaluation.getCreatedAt(),
                evaluation.getUpdatedAt()
        );
    }
}
