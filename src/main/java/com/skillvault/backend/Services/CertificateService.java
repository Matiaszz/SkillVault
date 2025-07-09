package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Certificate;
import com.skillvault.backend.Domain.Enums.EvalResult;
import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Domain.Skill;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Repositories.CertificateRepository;
import com.skillvault.backend.Repositories.SkillRepository;
import com.skillvault.backend.Repositories.UserRepository;
import com.skillvault.backend.dtos.Requests.CertificateRequestDTO;
import com.skillvault.backend.dtos.Responses.CertificateResponseDTO;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CertificateService {
    private final CertificateRepository certificateRepository;
    private final SkillService skillService;
    private final AzureService azureService;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

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
                    .status(EvalResult.PENDING)
                    .build();


            certificateRepository.save(cert);

            String blobName = cert.getId() + "_" + file.getOriginalFilename();
            cert.setBlobName(blobName);

            azureService.uploadCertificate(blobName, bytes);

            certificateRepository.save(cert);

            emailService.notifyEvaluators(cert);
            return new CertificateResponseDTO(cert);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process file");
        }
    }

    public ByteArrayResource downloadCertificate(UUID certificateId){
        Certificate certificate = getCertificateById(certificateId);

       return azureService.downloadCertificate(certificate.getBlobName());
    }


    @Transactional
    public CertificateResponseDTO updateCertificate(UUID id, MultipartFile file, CertificateRequestDTO data) {
        User user = tokenService.getLoggedEntity();
        Certificate certificate = getCertificateById(id);

        try {
            byte[] bytes = file.getBytes();

            if (!user.getId().equals(certificate.getUser().getId()) && !user.getRole().equals(UserRole.ADMIN)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't update others people's certificates");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must have a valid name");
            }

            List<Skill> oldSkills = List.copyOf(certificate.getRequestedSkills());

            for (Skill skill : oldSkills) {
                skillService.deleteSkillIfExists(skill.getId());
            }

            certificate.getRequestedSkills().clear();

            List<Skill> newSkills = data.skills().stream()
                    .map(dto -> skillService.registerSkill(user, dto))
                    .collect(Collectors.toList());

            certificate.getRequestedSkills().addAll(newSkills);

            String blobName = certificate.getId() + "_" + originalFilename;
            azureService.deleteByBlobName(certificate.getBlobName());
            azureService.uploadCertificate(blobName, bytes);
            certificate.setBlobName(blobName);

            certificate.setName(data.name());
            certificate.setStatus(EvalResult.PENDING);

            certificateRepository.save(certificate);
            return new CertificateResponseDTO(certificate);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process file");
        }
    }


    @Transactional
    public void deleteCertificate(UUID id){
        User user = tokenService.getLoggedEntity();
        Certificate certificate = getCertificateById(id);

        if (!user.getId().equals(certificate.getUser().getId()) && !user.getRole().equals(UserRole.ADMIN)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't delete others people certificates");
        }

        for (Skill skill: certificate.getRequestedSkills()){
            skillService.deleteSkillIfExists(skill.getId());
            user.getSkills().remove(skill);
        }
        userRepository.save(user);
        userRepository.flush();


        azureService.deleteByBlobName(certificate.getBlobName());
        certificateRepository.delete(certificate);
    }

    public Certificate getCertificateById(UUID id){
        return certificateRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certificate ID not found"));
    }
}
