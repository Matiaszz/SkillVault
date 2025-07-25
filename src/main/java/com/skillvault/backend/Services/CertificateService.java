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
import com.skillvault.backend.dtos.Requests.UpdateCertificateDTO;
import com.skillvault.backend.dtos.Responses.CertificateResponseDTO;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static com.skillvault.backend.Utils.FileUtils.validateCertificateExtension;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class CertificateService {
    private final CertificateRepository certificateRepository;
    private final SkillService skillService;
    private final AzureService azureService;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final NotificationService notificationService;

    @Transactional
    public CertificateResponseDTO uploadCertificate(CertificateRequestDTO data){
        log.warn("=== SKILLS === : {}",data.skills());
        if (data.skills() == null || data.skills().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A certificate must have skills");
        }

        User user = tokenService.getLoggedEntity();

        List<Skill> skills = data.skills().stream().map(dto -> skillService.registerSkill(user, dto))
                .toList();

        Certificate certificate = Certificate.builder()
                .user(user)
                .name(data.name())
                .requestedSkills(skills)
                .status(EvalResult.PENDING)
                .isFeatured(data.isFeatured() != null ? data.isFeatured() : false)
                .build();

        Certificate cert = certificateRepository.save(certificate);

        return new CertificateResponseDTO(cert);
    }

    @Transactional
    public CertificateResponseDTO uploadCertificateToAzure(MultipartFile file, UUID certificateId){
        Certificate certificate = certificateRepository.findById(certificateId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certificate not found."));

        if (!isValidFile(file)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Certificate must be an Image, Docx or PDF");
        }

        try {
            byte[] bytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();
            log.warn("{}", originalFilename);

            if (originalFilename == null || originalFilename.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file: missing filename");
            }

            String blobName = certificateId + "_" + originalFilename;
            log.warn("{}", blobName);
            if (certificate.getBlobName() != null) azureService.deleteByBlobName(certificate.getBlobName());

            azureService.uploadCertificate(blobName, bytes);

            certificate.setBlobName(blobName);
            notificationService.notifyByRoleAboutCertificate(certificate, UserRole.EVALUATOR);
            return new CertificateResponseDTO(certificate);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error on file processing");
        }
    }

    @Transactional
    public Certificate updateCertificateData(UUID id, UpdateCertificateDTO data) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certificate not found for update."));

        if (data.name() != null && !data.name().isBlank()) {
            certificate.setName(data.name());
        }

        if (data.isFeatured() != null) {
            certificate.setFeatured(data.isFeatured());
        }

        return certificateRepository.save(certificate);
    }

    public ByteArrayResource downloadCertificate(UUID certificateId){
        Certificate certificate = getCertificateById(certificateId);

       return azureService.downloadCertificate(certificate.getBlobName());
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
            user.removeSkill(skill);
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

    public Page<Certificate> getCertificatesByUser(UUID userId, Pageable pageable) {
        boolean exists = userRepository.existsById(userId);

        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        return certificateRepository.findByUserId(userId, pageable);
    }

    public EvalResult determineEvalResult(List<Skill> approvedSkills, List<Skill> reprovedSkills) {
        boolean hasApproved = approvedSkills != null && !approvedSkills.isEmpty();
        boolean hasReproved = reprovedSkills != null && !reprovedSkills.isEmpty();

        if (hasReproved && !hasApproved) return EvalResult.REJECTED;
        if (hasReproved) return EvalResult.PARTIALLY_APPROVED;
        return EvalResult.APPROVED;
    }

    private boolean isValidFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must have a valid name");
        }
        return validateCertificateExtension(file);

    }


}
