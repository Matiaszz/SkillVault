package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Certificate;
import com.skillvault.backend.Domain.Enums.SkillStatus;
import com.skillvault.backend.Domain.Skill;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Repositories.SkillRepository;
import com.skillvault.backend.dtos.Requests.SkillRequestDTO;
import com.skillvault.backend.dtos.Requests.UpdateSkillDTO;
import com.skillvault.backend.dtos.Responses.SkillResponseDTO;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final TokenService tokenService;

    @Transactional
    public Skill registerSkill(User user, SkillRequestDTO skillDTO) {
        String skillName = skillDTO.name();

        boolean exists = skillRepository.existsByUserAndNameIgnoreCase(user, skillName);
        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "A skill with the name: '" + skillName + "' already exists");
        }

        Skill skill = new Skill(skillDTO, user);
        Skill savedSkill = skillRepository.save(skill);
        skillRepository.flush();

        return savedSkill;
    }


    @Transactional
    public void deleteSkillIfExists(UUID skillId) {
        Skill skill = skillRepository.findById(skillId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill ID can't be found"));

        User user = tokenService.getLoggedEntity();

        if (!user.getId().equals(skill.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't delete others people's skills");
        }

        user.getSkills().remove(skill);

        skillRepository.delete(skill);
    }

    public Skill updateSkill(UpdateSkillDTO dto, UUID id) {
        User user = tokenService.getLoggedEntity();
        if (user == null) throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "To access this area, you must be authenticated");

        if (dto == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing skill data for update");

        Skill skill = skillRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));

        if (user.getId() != skill.getUser().getId()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You aren't allowed to access this skill");
        }


        if (dto.name() != null && !dto.name().isBlank()) skill.setName(dto.name());
        if (dto.description() != null && !dto.description().isBlank()) skill.setDescription(dto.description());
        Skill savedSkill = skillRepository.save(skill);
        skillRepository.flush();
        return savedSkill;
    }

    public Skill getSkillById(UUID id){
        return skillRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));
    }

    public Skill changeSkillStatus(SkillResponseDTO dto, SkillStatus status){
        Skill s = skillRepository.findById(dto.id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found."));
        s.setStatus(status);
        skillRepository.save(s);
        skillRepository.flush();
        return s;
    }

    public void verifyLinkBetweenSkillAndCertificate(List<SkillResponseDTO> skills, Certificate certificate){
        for (SkillResponseDTO skillDto : skills) {
            Skill skill = getSkillById(skillDto.id());
            if (!certificate.getRequestedSkills().contains(skill)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "The skill: " + skill.getName() + " is not linked to certificate '" + certificate.getName() + "'."
                );
            }
        }
    }

}
