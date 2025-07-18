package com.skillvault.backend.dtos.Requests;


import com.skillvault.backend.Validations.Password.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(

        @NotBlank(message = "Username is required")
        @Size(max=17, message = "Username must have at most 17 characters")
        String username,

        @NotBlank(message = "The name is required")
        String name,

        @NotBlank
        @Email(message = "Invalid E-Mail")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be 6 characters or more")
        @Password
        String password
) {
}
