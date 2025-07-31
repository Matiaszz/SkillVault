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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
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
        userMock1 = utils.authenticateTest(UserRole.USER, "mock1", "mock1@email.com");
    }
    @AfterEach
    void tearDown() throws Exception{
        userMock1 = null;
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
    @DisplayName("Should delete the skill from DB")
    void deleteSkillIfExistsSuccess() {
        Certificate certificate = createCertificate();
        Skill skill = registerSkill(certificate, userMock1);
        skillService.deleteSkillIfExists(skill.getId());

        boolean skillExists = skillRepository.existsById(skill.getId());
        assertThat(!skillExists).isTrue();
    }

    @Test
    @DisplayName("Shouldn't delete the skill from DB because ID doesn't exists")
    void deleteSkillIfExistsFailureNotFound() {
        assertThatThrownBy(() -> skillService.deleteSkillIfExists(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Skill ID can't be found");
    }

    @Test
    @DisplayName("Shouldn't delete the skill from DB because the user aren't the skill owner")
    void deleteSkillIfExistsFailurePermission() {
        Certificate certificate = createCertificate();
        Skill skill = registerSkill(certificate, userMock1);
        utils.authenticateTest(UserRole.USER, "mock2", "mock2@email.com");

        assertThatThrownBy(() -> skillService.deleteSkillIfExists(skill.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You can't delete others people's skills");
    }

    @Test
    @DisplayName("Should update the skill successfully")
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
    @DisplayName("Shouldn't update the skill from DB because ID doesn't exists")
    void updateSkillFailureNotFound() {;
        UpdateSkillDTO data = new UpdateSkillDTO("Changed", null, null);
        assertThatThrownBy(() -> skillService.updateSkill(data, UUID.randomUUID())).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Skill not found");
    }

    @Test
    @DisplayName("Shouldn't update the skill from DB because the logged user aren't the owner of the skill")
    void updateSkillFailurePermission() {;
        Certificate certificate = createCertificate();
        Skill skill = registerSkill(certificate, userMock1);
        utils.authenticateTest(UserRole.USER, "mock2", "mock2@email.com");

        UpdateSkillDTO data = new UpdateSkillDTO("Changed", null, null);
        assertThatThrownBy(() -> skillService.updateSkill(data, skill.getId())).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You aren't allowed to access this skill");
    }

    @Test
    @DisplayName("Should update the skill from DB because the logged user is an admin")
    void updateSkillSuccessPermission() {;
        Certificate certificate = createCertificate();
        Skill skill = registerSkill(certificate, userMock1);
        utils.authenticateTest(UserRole.ADMIN, "adm", "adm@email.com");

        UpdateSkillDTO data = new UpdateSkillDTO("Changed", null, null);
        skillService.updateSkill(data, skill.getId());
        Optional<Skill> foundSkill = skillRepository.findById(skill.getId());

        assertThat(foundSkill.isPresent()).isTrue();
        assertThat(foundSkill.get().getName().equals("Changed")).isTrue();
    }


    @Test
    void getUserSkills() {
        Certificate certificate = createCertificate();
        registerSkill(certificate, userMock1);
        Page<Skill> skills = skillService.getUserSkills(userMock1.getId(), Pageable.unpaged());
        assertThat(skills).isNotEmpty();

        assertThat(skills.getContent()).allMatch(skill -> skill.getUser().getId().equals(userMock1.getId()));
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