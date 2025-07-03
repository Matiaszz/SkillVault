package com.skillvault.backend.dtos.Requests;

import jakarta.validation.constraints.Email;

public record UpdateUserDTO(
        String name,

        @Email(message = "Invalid E-Mail")
        String email,

        String biography
) {
}
