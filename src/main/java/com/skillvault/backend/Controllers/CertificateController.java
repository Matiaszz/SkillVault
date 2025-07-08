package com.skillvault.backend.Controllers;

import com.skillvault.backend.Domain.Certificate;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Services.AzureService;
import com.skillvault.backend.Services.CertificateService;
import com.skillvault.backend.Services.EmailService;
import com.skillvault.backend.Services.TokenService;
import com.skillvault.backend.dtos.Requests.CertificateRequestDTO;
import com.skillvault.backend.dtos.Responses.CertificateResponseDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

import static com.skillvault.backend.Utils.FileUtils.validateCertificateExtension;

@RestController
@RequestMapping("/api/certificate")
@AllArgsConstructor
public class CertificateController {

    private final TokenService tokenService;
    private final CertificateService certificateService;
    private final EmailService emailService;


    @PostMapping
    public ResponseEntity<CertificateResponseDTO> uploadCertificate(
            @RequestParam("file") MultipartFile file,
            @ModelAttribute @Valid CertificateRequestDTO data
    ) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Must have a certificate file");
        }

        if (!validateCertificateExtension(file)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "The certificate must be an Image, DOCX or PDF");
        }

        User user = tokenService.getLoggedEntity();
        CertificateResponseDTO response = certificateService.uploadCertificateWithData(user, file, data);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{certificateId}")
    public ResponseEntity<CertificateResponseDTO> getCertificateById(@PathVariable UUID certificateId){
        Certificate certificate = certificateService.getCertificateById(certificateId);
        CertificateResponseDTO response = new CertificateResponseDTO(certificate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{certificateId}")
    public ResponseEntity<ByteArrayResource> downloadCertificate(@PathVariable UUID certificateId){

        ByteArrayResource resource = certificateService.downloadCertificate(certificateId);
        Certificate certificate = certificateService.getCertificateById(certificateId);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + certificate.getBlobName()  + "\"")
                .body(resource);
    }

    @DeleteMapping("/{certificateId}")
    public ResponseEntity<Void> deleteCertificate(@PathVariable UUID certificateId){
        certificateService.deleteCertificate(certificateId);
        return ResponseEntity.noContent().build();
    }




}
