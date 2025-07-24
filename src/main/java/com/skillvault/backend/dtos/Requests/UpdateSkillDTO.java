package com.skillvault.backend.dtos.Requests;

import com.skillvault.backend.Domain.Skill;

public record UpdateSkillDTO (String name, String description, Boolean isFeatured) {
    public UpdateSkillDTO(Skill skill){
        this(skill.getName(), skill.getDescription(), skill.isFeatured());
    }

}
