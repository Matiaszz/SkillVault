package com.skillvault.backend.Services;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.skillvault.backend.Domain.Certificate;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Domain.UserProfilePicture;
import com.skillvault.backend.Repositories.CertificateRepository;
import com.skillvault.backend.Repositories.UserProfilePictureRepository;
import com.skillvault.backend.Repositories.UserRepository;
import com.skillvault.backend.dtos.Responses.CertificateResponseDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class AzureService {
    private final BlobContainerClient certificateContainerClient;
    private final BlobContainerClient profilePictureContainerClient;
    private final UserRepository userRepository;
    private final CertificateRepository certificateRepository;

    public AzureService(
            UserProfilePictureRepository profilePictureRepository,
            UserRepository userRepository,
            CertificateRepository certificateRepository,
            @Value("${azure.storage.connection-string}") String connectionString,
            @Value("${azure.storage.certificate.container-name}") String certificateContainer,
            @Value("${azure.storage.profile-picture.container-name}") String profilePictureContainer

    ) {

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString).buildClient();

        this.userRepository = userRepository;
        this.certificateRepository = certificateRepository;

        this.profilePictureContainerClient = serviceClient.getBlobContainerClient(profilePictureContainer);
        this.certificateContainerClient = serviceClient.getBlobContainerClient(certificateContainer);

        if (!profilePictureContainerClient.exists()) {
            profilePictureContainerClient.create();
        }

        if (!certificateContainerClient.exists()) {
            certificateContainerClient.create();
        }
    }

    @Transactional
    public void uploadProfilePicture(User user, MultipartFile file) {
        try {
            byte[] data = file.getBytes();

            UserProfilePicture profilePicture = user.getProfilePicture();
            if (profilePicture == null) {
                profilePicture = new UserProfilePicture();
                profilePicture.setId(UUID.randomUUID());
                profilePicture.setUser(user);
            }

            String blobId = profilePicture.getId() + "_" + file.getOriginalFilename();
            getPictureBlobClient(profilePicture).upload(new ByteArrayInputStream(data), data.length, true);


            profilePicture.setBlobName(blobId);
            user.setProfilePicture(profilePicture);
            userRepository.save(user);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error on picture reading.");
        }
    }

    public void uploadToBlob(String blobName, byte[] data) {
        getCertificateBlobClient(blobName)
                .upload(new ByteArrayInputStream(data), data.length, true);
    }

    private BlobClient getCertificateBlobClient(String blobName) {
        return certificateContainerClient.getBlobClient(blobName);
    }

    private BlobClient getPictureBlobClient(UserProfilePicture picture) {
        String uniqueBlobName = picture.getBlobName();
        return profilePictureContainerClient.getBlobClient(uniqueBlobName);
    }
}
