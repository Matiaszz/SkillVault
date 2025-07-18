package com.skillvault.backend.Services;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Domain.UserProfilePicture;
import com.skillvault.backend.Repositories.UserProfilePictureRepository;
import com.skillvault.backend.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
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
    private final UserProfilePictureRepository userProfilePictureRepository;

    public AzureService(
            UserProfilePictureRepository profilePictureRepository,
            @Value("${azure.storage.connection-string}") String connectionString,
            @Value("${azure.storage.certificate.container-name}") String certificateContainer,
            @Value("${azure.storage.profile-picture.container-name}") String profilePictureContainer
    ) {

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString).buildClient();

        this.userProfilePictureRepository = profilePictureRepository;
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
                profilePicture.setUser(user);
                user.setProfilePicture(profilePicture);
                userProfilePictureRepository.save(profilePicture);
            } else {
                if (profilePicture.getBlobName() != null) {
                    getPictureBlobClient(profilePicture.getBlobName()).deleteIfExists();
                }
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = "default";
            }

            String blobId = profilePicture.getId() + "_" + fileName.trim().replaceAll(" ", "");
            profilePicture.setBlobName(blobId);

            getPictureBlobClient(blobId).upload(new ByteArrayInputStream(data), data.length, true);

            userProfilePictureRepository.save(profilePicture);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error on picture reading.");
        }
    }


    public ByteArrayResource downloadCertificate(String blobName){
        byte[] data = getCertificateBlobClient(blobName).downloadContent().toBytes();
        return new ByteArrayResource(data);
    }

    public ByteArrayResource downloadProfilePicture(String blobName){
        byte[] data = getPictureBlobClient(blobName).downloadContent().toBytes();
        return new ByteArrayResource(data);
    }

    public void uploadCertificate(String blobName, byte[] data) {
        getCertificateBlobClient(blobName)
                .upload(new ByteArrayInputStream(data), data.length, true);
    }

    public void deleteByBlobName(String blobName){
        getCertificateBlobClient(blobName).deleteIfExists();
    }

    private BlobClient getCertificateBlobClient(String blobName) {
        return certificateContainerClient.getBlobClient(blobName);
    }

    private BlobClient getPictureBlobClient(String blobName) {
        return profilePictureContainerClient.getBlobClient(blobName);
    }
}
