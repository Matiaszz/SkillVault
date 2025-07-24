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
import com.skillvault.backend.dtos.Requests.NotificationRequestDTO;
import com.skillvault.backend.dtos.Responses.CertificateResponseDTO;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static com.skillvault.backend.Utils.FileUtils.validateCertificateExtension;

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
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
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
                    .isFeatured(data.isFeatured() != null ? data.isFeatured() : false)
                    .build();


            certificateRepository.save(cert);

            String blobName = cert.getId() + "_" + file.getOriginalFilename();
            cert.setBlobName(blobName);

            azureService.uploadCertificate(blobName, bytes);

            certificateRepository.save(cert);
            notificationService.notifyByRoleAboutCertificate(cert, UserRole.EVALUATOR);
            return new CertificateResponseDTO(cert);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process file");
        }
    }

    @Transactional
    public void updateCertificateAzure(MultipartFile file, UUID certificateId){
        Certificate certificate = certificateRepository.findById(certificateId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certificate not found."));

        if (!isValidFile(file)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Certificate must be an Image, Docx or PDF");
        }

        try {
            byte[] bytes = file.getBytes();
            String blobName = certificateId + "_" + file.getOriginalFilename();
            azureService.deleteByBlobName(certificate.getBlobName());
            azureService.uploadCertificate(blobName, bytes);
            certificate.setBlobName(blobName);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error on file processing");
        }
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
