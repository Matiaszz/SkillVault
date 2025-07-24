package com.skillvault.backend.dtos.Responses;

import com.skillvault.backend.Domain.Skill;

import java.time.LocalDateTime;
import java.util.UUID;

public record SkillResponseDTO(
        UUID id,
        String name,
        String description,
        String status,
        Boolean isFeatured,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public SkillResponseDTO(Skill skill){
        this(
                skill.getId(),
                skill.getName(),
                skill.getDescription(),
                skill.getStatus().toString(),
                skill.isFeatured(),
                skill.getCreatedAt(),
                skill.getUpdatedAt()
        );
    }
}
