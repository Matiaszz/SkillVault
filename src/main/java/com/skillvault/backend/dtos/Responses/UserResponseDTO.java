package com.skillvault.backend.dtos.Responses;

import com.skillvault.backend.Domain.Skill;
import com.skillvault.backend.Domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserResponseDTO(

        UUID id,
        String username,

        String name,

        String email,

        String biography,

        List<SkillResponseDTO> skills,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
    public UserResponseDTO(User user){
        this(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getBiography(),
                user.getSkills().stream().map(SkillResponseDTO::new).toList(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
