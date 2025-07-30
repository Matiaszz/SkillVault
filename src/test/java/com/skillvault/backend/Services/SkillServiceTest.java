package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Certificate;
import com.skillvault.backend.Domain.Enums.UserRole;
import com.skillvault.backend.Domain.Skill;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Repositories.CertificateRepository;
import com.skillvault.backend.Repositories.SkillRepository;
import com.skillvault.backend.Repositories.UserRepository;
import com.skillvault.backend.Utils.TestUtils;
import com.skillvault.backend.config.TestConfig;
import com.skillvault.backend.dtos.Requests.SkillRequestDTO;
import com.skillvault.backend.dtos.Requests.UpdateSkillDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class SkillServiceTest {

    private AutoCloseable closeable;
    private User userMock1;
    private User userMock2;
    private User adminMock;

    @Mock
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private CertificateService certificateService;


    @Autowired
    @InjectMocks
    private SkillService skillService;

    @Autowired
    private CertificateRepository certificateRepository;


    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private TestUtils utils;

    @BeforeEach
    void setup(){
        closeable = MockitoAnnotations.openMocks(this);
        userMock2 = utils.authenticateTest(UserRole.USER, "mock2", "mock2@email.com");
        adminMock = utils.authenticateTest(UserRole.ADMIN, "admin", "admin@email.com");
        userMock1 = utils.authenticateTest(UserRole.USER, "mock1", "mock1@email.com");
    }
    @AfterEach
    void tearDown() throws Exception{
        userMock1 = null;
        userMock2 = null;
        adminMock = null;
        this.userRepository.deleteAll();
        closeable.close();
    }

    @Test
    @DisplayName("Should register a skill in DB")
    void registerSkillSuccess() {
        Certificate certificate = createCertificate();
        SkillRequestDTO data = new SkillRequestDTO(certificate.getId().toString(), "Skill", "Skill desc", true);
        Skill sk = skillService.registerSkill(this.userMock1, data);

        boolean exists = skillRepository.existsById(sk.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should throw the conflict response status")
    void registerSkillConflict() {
        Certificate certificate = createCertificate();

        SkillRequestDTO request = makeSkill(certificate);
        skillService.registerSkill(userMock1, request);

        assertThatThrownBy(() -> skillService.registerSkill(userMock1, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should delete a skill from DB")
    void deleteSkillIfExistsSuccess() {
        Certificate certificate = createCertificate();
        Skill skill = registerSkill(certificate, userMock1);
        skillService.deleteSkillIfExists(skill.getId());

        boolean skillExists = skillRepository.existsById(skill.getId());
        assertThat(!skillExists).isTrue();
    }

    @Test
    @DisplayName("Shouldn't delete a skill from DB because ID doesn't exists")
    void deleteSkillIfExistsFailureNotFound() {
        assertThatThrownBy(() -> skillService.deleteSkillIfExists(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Skill ID can't be found");
    }

    @Test
    @DisplayName("Should update skill successfully")
    void updateSkillSuccess() {
        Certificate certificate = createCertificate();
        Skill skill = registerSkill(certificate, userMock1);
        UpdateSkillDTO data = new UpdateSkillDTO("Changed", null, null);
        skillService.updateSkill(data, skill.getId());
        Optional<Skill> foundSkill = skillRepository.findById(skill.getId());

        assertThat(foundSkill.isPresent()).isTrue();
        assertThat(foundSkill.get().getName().equals("Changed")).isTrue();
    }

    @Test
    @DisplayName("Shouldn't update skill from DB because ID doesn't exists")
    void updateSkillFailureNotFound() {;
        UpdateSkillDTO data = new UpdateSkillDTO("Changed", null, null);
        assertThatThrownBy(() -> skillService.updateSkill(data, UUID.randomUUID())).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Skill not found");
    }

    @Test
    void getUserSkills() {
    }

    @Test
    void getSkillById() {
    }

    @Test
    void changeSkillStatus() {
    }

    private Certificate createCertificate(){
        Certificate c = new Certificate();
        return certificateRepository.save(c);
    }

    private Skill registerSkill(Certificate certificate, User userMock){
        SkillRequestDTO data = makeSkill(certificate);
        return skillService.registerSkill(userMock, data);
    }

    private SkillRequestDTO makeSkill(Certificate certificate){
        return new SkillRequestDTO(certificate.getId().toString(),
                "Skill", "Skill desc", true);
    }
}