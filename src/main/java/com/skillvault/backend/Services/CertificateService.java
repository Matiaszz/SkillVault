package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Certificate;
import com.skillvault.backend.Domain.Skill;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Repositories.CertificateRepository;
import com.skillvault.backend.dtos.Requests.CertificateRequestDTO;
import com.skillvault.backend.dtos.Responses.CertificateResponseDTO;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CertificateService {
    private final CertificateRepository certificateRepository;
    private final SkillService skillService;
    private final AzureService azureService;

    @Transactional
    public CertificateResponseDTO uploadCertificateWithData(User user, MultipartFile file, CertificateRequestDTO data) {
        try {
            byte[] bytes = file.getBytes();


            List<Skill> skills = data.skills().stream()
                    .map(dto -> skillService.registerSkill(user, dto))
                    .collect(Collectors.toList());

            Certificate cert = Certificate.builder()
                    .user(user)
                    .name(data.name())
                    .requestedSkills(skills)
                    .build();


            certificateRepository.save(cert);

            String blobName = cert.getId() + "_" + file.getOriginalFilename();
            cert.setBlobName(blobName);

            azureService.uploadToBlob(blobName, bytes);

            certificateRepository.save(cert);

            return new CertificateResponseDTO(cert);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process file");
        }
    }
}
