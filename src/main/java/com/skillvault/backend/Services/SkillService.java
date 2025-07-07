package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Skill;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Repositories.SkillRepository;
import com.skillvault.backend.dtos.Requests.SkillRequestDTO;
import com.skillvault.backend.dtos.Requests.UpdateSkillDTO;
import com.skillvault.backend.dtos.Responses.SkillResponseDTO;
import jakarta.persistence.EntityManager;
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

    public Skill registerSkill(User user, SkillRequestDTO skillDTO){
        String skillName = skillDTO.name();

        boolean exists = user.getSkills().stream()
                .anyMatch(skill -> skill.getName().equalsIgnoreCase(skillName));

        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "A skill with the name: '" + skillName + "' already exists");
        }

        Skill skill = new Skill(skillDTO, user);
        Skill savedSkill = skillRepository.save(skill);
        return savedSkill;
    }

    public void deleteSkillIfExists(UUID skillId){
        Skill skill = skillRepository.findById(skillId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill ID can't be found"));

        User user = tokenService.getLoggedEntity();

        if (user.getId() != skill.getUser().getId()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill ID can't be found");
        }
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


        return skillRepository.save(skill);
    }

}
