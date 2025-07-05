package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Skill;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Repositories.SkillRepository;
import com.skillvault.backend.dtos.Requests.SkillRequestDTO;
import com.skillvault.backend.dtos.Responses.SkillResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@AllArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final TokenService tokenService;

    public SkillResponseDTO registerSkill(User user, SkillRequestDTO skillDTO){
        Skill skill = new Skill(skillDTO, user);
        Skill savedSkill = skillRepository.save(skill);
        return new SkillResponseDTO(savedSkill);
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

    private boolean skillExistsById(UUID skillId){
        return skillRepository.existsById(skillId);
    }
}
