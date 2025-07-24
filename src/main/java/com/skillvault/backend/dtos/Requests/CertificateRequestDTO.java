package com.skillvault.backend.dtos.Requests;


import java.util.List;

public record CertificateRequestDTO (String name,List<SkillRequestDTO> skills, Boolean isFeatured ) {

}
