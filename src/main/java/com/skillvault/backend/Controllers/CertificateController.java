package com.skillvault.backend.Controllers;

import com.skillvault.backend.Domain.Certificate;
import com.skillvault.backend.Services.CertificateService;
import com.skillvault.backend.Services.TokenService;
import com.skillvault.backend.dtos.Requests.CertificateRequestDTO;
import com.skillvault.backend.dtos.Requests.UpdateCertificateDTO;
import com.skillvault.backend.dtos.Responses.CertificateResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static com.skillvault.backend.Utils.FileUtils.validateCertificateExtension;

@RestController
@RequestMapping("/api/certificate")
@AllArgsConstructor
public class CertificateController {

    private final TokenService tokenService;
    private final CertificateService certificateService;

    @Operation(summary = "Get logged-in user's certificates", description = "Returns a paginated list of certificates that belong to the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificates retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No certificates found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my")
    public ResponseEntity<Page<CertificateResponseDTO>> getUserCertificates(Pageable pageable) {
        UUID userId = tokenService.getLoggedEntity().getId();
        Page<CertificateResponseDTO> dtoPage = getPageByUserId(userId, pageable);
        return ResponseEntity.ok(dtoPage);
    }

    @Operation(summary = "Get logged-in user's certificates", description = "Returns a paginated list of certificates that belong to the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificates retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No certificates found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<Page<CertificateResponseDTO>> getCertificatesByUserId(@PathVariable UUID userId, Pageable pageable){
        Page<CertificateResponseDTO> dtoPage = getPageByUserId(userId, pageable);
        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping
    public ResponseEntity<CertificateResponseDTO> uploadCertificate(@RequestBody CertificateRequestDTO data){
        CertificateResponseDTO certificate = certificateService.uploadCertificate(data);
        return ResponseEntity.ok(certificate);
    }

    @Operation(summary = "Get certificate by ID", description = "Returns the certificate's details by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificate retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Certificate not found")
    })
    @GetMapping("/{certificateId}")
    public ResponseEntity<CertificateResponseDTO> getCertificateById(@PathVariable UUID certificateId){
        Certificate certificate = certificateService.getCertificateById(certificateId);
        CertificateResponseDTO response = new CertificateResponseDTO(certificate);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Download certificate file", description = "Downloads the uploaded certificate file (PDF, DOCX, or image).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "Certificate not found")
    })
    @GetMapping("/download/{certificateId}")
    public ResponseEntity<ByteArrayResource> downloadCertificate(@PathVariable UUID certificateId){
        ByteArrayResource resource = certificateService.downloadCertificate(certificateId);
        Certificate certificate = certificateService.getCertificateById(certificateId);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + certificate.getBlobName() + "\"")
                .body(resource);
    }

    @Deprecated
    @Operation(summary = "Update certificate", description = "Updates an existing certificate's file and skill data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificate updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or input"),
            @ApiResponse(responseCode = "403", description = "User not authorized to update this certificate"),
            @ApiResponse(responseCode = "404", description = "Certificate not found")
    })

    @PatchMapping("/{id}")
    public ResponseEntity<CertificateResponseDTO> updateCertificateData(@PathVariable UUID id, @RequestBody UpdateCertificateDTO data){
        Certificate certificate = certificateService.updateCertificateData(id, data);
        CertificateResponseDTO response = new CertificateResponseDTO(certificate);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/azure/update/{certificateId}")
    public ResponseEntity<CertificateResponseDTO> updateCertificateFile(
            @PathVariable UUID certificateId,
            @RequestParam("file") MultipartFile file){

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Must have a certificate file");
        }

        if (!validateCertificateExtension(file)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The certificate must be an Image, DOCX or PDF");
        }

        CertificateResponseDTO response = certificateService.uploadCertificateToAzure(file, certificateId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete certificate", description = "Deletes a certificate from the system including file and associated skills.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Certificate deleted successfully"),
            @ApiResponse(responseCode = "403", description = "User not authorized to delete this certificate"),
            @ApiResponse(responseCode = "404", description = "Certificate not found")
    })
    @DeleteMapping("/{certificateId}")
    public ResponseEntity<Void> deleteCertificate(@PathVariable UUID certificateId){
        certificateService.deleteCertificate(certificateId);
        return ResponseEntity.noContent().build();
    }

    private Page<CertificateResponseDTO> getPageByUserId(UUID userId, Pageable pageable){
        Page<Certificate> certificatesPage = certificateService.getCertificatesByUser(userId, pageable);
        return certificatesPage.map(CertificateResponseDTO::new);
    }
}
