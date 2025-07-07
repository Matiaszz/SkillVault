package com.skillvault.backend.dtos.Requests;

import com.skillvault.backend.Domain.Skill;

public record UpdateSkillDTO (String name, String description) {
    public UpdateSkillDTO(Skill skill){
        this(skill.getName(), skill.getDescription());
    }

}
