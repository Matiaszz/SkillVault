package com.skillvault.backend.Repositories;

import com.skillvault.backend.Domain.UserProfilePicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfilePictureRepository extends JpaRepository<UserProfilePicture, UUID> {
    Optional<UserProfilePicture> findByUser_Id(UUID uuid);
}
