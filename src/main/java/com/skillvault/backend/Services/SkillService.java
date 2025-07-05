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

    public SkillResponseDTO registerSkill(User user, SkillRequestDTO skillDTO){
        Skill skill = new Skill(skillDTO, user);
        Skill savedSkill = skillRepository.save(skill);
        return new SkillResponseDTO(savedSkill);
    }

    public void deleteSkillIfExists(UUID skillId){
        if (!skillExistsById(skillId)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill ID can't be found");
        }
        skillRepository.deleteById(skillId);

    }

    private boolean skillExistsById(UUID skillId){
        return skillRepository.existsById(skillId);
    }
}
