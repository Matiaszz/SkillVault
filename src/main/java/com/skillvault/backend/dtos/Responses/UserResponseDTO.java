package com.skillvault.backend.dtos.Responses;

import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Domain.UserProfilePicture;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserResponseDTO(

        UUID id,
        String username,
        UserRole role,
        String name,
        String email,
        String biography,
        List<SkillResponseDTO> skills,
        List<CertificateResponseDTO> certificates,
        UserProfilePicture profilePicture,
        String linkedin,
        String github,
        String site,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public UserResponseDTO(User user){
        this(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getName(),
                user.getEmail(),
                user.getBiography(),
                user.getSkills().stream().map(SkillResponseDTO::new).toList(),
                user.getCertificates().stream().map(CertificateResponseDTO::new).toList(),
                user.getProfilePicture(),
                user.getLinkedin(),
                user.getGithub(),
                user.getSite(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
