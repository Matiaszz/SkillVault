package com.skillvault.backend.dtos.Responses;

import com.skillvault.backend.Domain.User;

import java.util.UUID;

public record UserSimpleDTO(
        UUID id,
        String name,
        String username,
        String email
) {
    public UserSimpleDTO(User user) {
        this(user.getId(), user.getName(), user.getUsername(), user.getEmail());
    }
}
