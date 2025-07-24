package com.skillvault.backend.dtos.Requests;

import java.util.List;
import java.util.UUID;

public record UpdateCertificateDTO(
        String name, List<CertificateSkillUpdateDTO> skills, Boolean isFeatured
) {

}
