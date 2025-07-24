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
        Boolean isFeatured,
        UserSimpleDTO user,
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
                certificate.isFeatured(),
                new UserSimpleDTO(certificate.getUser()),
                certificate.getRequestedSkills() != null
                        ? certificate.getRequestedSkills().stream().map(SkillResponseDTO::new).toList()
                        : List.of(),
                certificate.getBlobName(),
                certificate.getCreatedAt(),
                certificate.getUpdatedAt()
        );
    }
}
