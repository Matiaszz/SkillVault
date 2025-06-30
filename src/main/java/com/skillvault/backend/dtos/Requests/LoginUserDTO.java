package com.skillvault.backend.dtos.Requests;

import jakarta.validation.constraints.Email;

public record LoginUserDTO(
        @Email
        String email,

        String password
) {
}
