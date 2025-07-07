package com.skillvault.backend.Controllers;

import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Services.AzureService;
import com.skillvault.backend.Services.CertificateService;
import com.skillvault.backend.Services.TokenService;
import com.skillvault.backend.dtos.Requests.CertificateRequestDTO;
import com.skillvault.backend.dtos.Responses.CertificateResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static com.skillvault.backend.Utils.FileUtils.validateCertificateExtension;
import static com.skillvault.backend.Validations.DTO.DTOValidator.validateCertificateRequestDTO;

@RestController
@RequestMapping("/api/certificate")
@AllArgsConstructor
public class CertificateController {

    private AzureService azureService;
    private TokenService tokenService;
    private CertificateService certificateService;

    @PostMapping("/full")
    public ResponseEntity<CertificateResponseDTO> uploadCertificateFull(
            @RequestParam("file") MultipartFile file,
            @ModelAttribute CertificateRequestDTO data
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


}
