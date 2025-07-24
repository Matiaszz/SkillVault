package com.skillvault.backend.dtos.Requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SkillRequestDTO(
        @NotBlank
        @Size(max=35, message = "Skill name must have at most 35 characters")
        String name,

        @NotBlank(message = "Skill descriptions must be specified")
        String description,

        Boolean featured
) {
        public SkillRequestDTO(CertificateSkillUpdateDTO dto) {
                this(dto.name(), dto.description(), dto.isFeatured());
        }
}
