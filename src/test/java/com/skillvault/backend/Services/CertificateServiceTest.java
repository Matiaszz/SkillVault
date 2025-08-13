package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Certificate;
import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Repositories.CertificateRepository;
import com.skillvault.backend.Repositories.UserRepository;
import com.skillvault.backend.config.TestConfig;
import com.skillvault.backend.dtos.Requests.CertificateRequestDTO;
import com.skillvault.backend.dtos.Requests.UpdateCertificateDTO;
import com.skillvault.backend.dtos.Responses.CertificateResponseDTO;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;


import com.skillvault.backend.Utils.TestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class CertificateServiceTest {
    private AutoCloseable closeable;


    @Mock
    private AzureService azureService;

    @Mock
    private TokenService tokenService;

    @Autowired  
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;


    @Autowired
    @InjectMocks
    private CertificateService certificateService;


    @Autowired
    private  CertificateRepository certificateRepository;

    @Autowired
    private TestUtils utils;

    @BeforeEach
    void setup(){
        closeable = MockitoAnnotations.openMocks(this);
        utils.authenticateTest(UserRole.USER, "UserName", "email@email.com");
    }
    @AfterEach
    void tearDown() throws Exception{
        this.userRepository.deleteAll();
        closeable.close();
    }

    @Test
    @DisplayName("Should upload a non-featured certificate to DB")
    void uploadCertificateNonFeatured() {

        CertificateRequestDTO data = new CertificateRequestDTO("certificate", false);
        CertificateResponseDTO certificate = certificateService.uploadCertificate(data);

        assertThat(certificate.isFeatured().equals(false)).isTrue();
    }

    @Test
    @DisplayName("Should upload a featured certificate to DB")
    void uploadCertificateFeatured() {
        CertificateResponseDTO certificate = createCertificate();

        assertThat(certificate.isFeatured().equals(true)).isTrue();
    }

    @Test
    @DisplayName("Should update a certificate data")
    void updateCertificateData() {
        UpdateCertificateDTO data = new UpdateCertificateDTO("Changed", null);
        CertificateResponseDTO certificate = createCertificate();

        Certificate updateCertificate = certificateService.updateCertificateData(certificate.id(), data);

        assertThat(updateCertificate.getName().equals(data.name())).isTrue();
    }

    @Test
    @DisplayName("Should update certificate with blank name and ignore name change")
    void updateCertificateWithBlankName_shouldIgnoreNameChange() {
        CertificateResponseDTO certificate = createCertificate();
        UpdateCertificateDTO data = new UpdateCertificateDTO("   ", true);

        Certificate updated = certificateService.updateCertificateData(certificate.id(), data);

        assertThat(updated.getName()).isEqualTo("certificate");
        assertThat(updated.isFeatured()).isTrue();
    }

    @Test
    @DisplayName("Should throw when updating non-existent certificate")
    void updateNonExistentCertificate_shouldThrow() {
        UUID fakeId = UUID.randomUUID();
        UpdateCertificateDTO data = new UpdateCertificateDTO("New Name", false);

        assertThatThrownBy(() -> certificateService.updateCertificateData(fakeId, data))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Certificate not found");
    }

    @Test
    @DisplayName("Should delete a certificate")
    void deleteCertificate() {
        CertificateResponseDTO certificate = createCertificate();
        certificateService.deleteCertificate(certificate.id());
        boolean exists = certificateRepository.existsById(certificate.id());

        assertThat(!exists).isTrue();
    }

    private CertificateResponseDTO createCertificate() {
        CertificateRequestDTO data = new CertificateRequestDTO("certificate", true);
        return certificateService.uploadCertificate(data);
    }
}