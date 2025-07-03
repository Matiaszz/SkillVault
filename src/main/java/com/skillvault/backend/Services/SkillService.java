package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Skill;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Repositories.SkillRepository;
import com.skillvault.backend.dtos.Requests.SkillRequestDTO;
import com.skillvault.backend.dtos.Responses.SkillResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillResponseDTO registerSkill(User user, SkillRequestDTO skillDTO){
        Skill skill = new Skill(skillDTO, user);
        Skill savedSkill = skillRepository.save(skill);
        return new SkillResponseDTO(savedSkill);
    }
}
