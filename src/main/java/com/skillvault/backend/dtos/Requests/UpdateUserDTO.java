package com.skillvault.backend.dtos.Requests;

import com.skillvault.backend.Domain.User;
import jakarta.validation.constraints.Email;

public record UpdateUserDTO(
        String name,

        @Email(message = "Invalid E-Mail")
        String email,

        String biography,
        String linkedin,
        String github,
        String site
) {
        public UpdateUserDTO (User user){
                this(
                        user.getName(),
                        user.getEmail(),
                        user.getBiography(),
                        user.getLinkedin(),
                        user.getGithub(),
                        user.getSite()
                );
        }
}
