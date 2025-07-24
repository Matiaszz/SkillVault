package com.skillvault.backend.dtos.Requests;

import java.util.UUID;

public record CertificateSkillUpdateDTO(
        String name,
        String description,
        Boolean isFeatured
) {
}
