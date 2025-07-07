package com.skillvault.backend.dtos.Responses;

import com.skillvault.backend.Domain.Certificate;
import com.skillvault.backend.Domain.Enums.EvalResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CertificateResponseDTO (
        UUID id,
        String name,
        EvalResult status,
        UserResponseDTO user,
        List<SkillResponseDTO> requiredSkills,
        String blobName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt


) {
    public CertificateResponseDTO(Certificate certificate){
        this(
                certificate.getId(),
                certificate.getName(),
                certificate.getStatus(),
                certificate.getUser() != null ? new UserResponseDTO(certificate.getUser()) : null,
                certificate.getRequestedSkills() != null
                        ? certificate.getRequestedSkills().stream().map(SkillResponseDTO::new).toList()
                        : List.of(),
                certificate.getBlobName(),
                certificate.getCreatedAt(),
                certificate.getUpdatedAt()
        );
    }
}
