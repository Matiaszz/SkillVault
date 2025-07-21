package com.skillvault.backend.dtos.Responses;

import com.skillvault.backend.Domain.User;

public record UserProfileResponseDTO(
        String id,
        String username,
        String profilePictureBlobName,
        String name,
        String email,
        String github,
        String linkedin,
        String site
) {
    public UserProfileResponseDTO(User user) {
        this(
                user.getId().toString(),
                user.getUsername(),
                user.getProfilePicture().getBlobName(),
                user.getName(),
                user.getEmail(),
                user.getGithub(),
                user.getLinkedin(),
                user.getSite()
        );
    }
}
