package com.skillvault.backend.Services;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Domain.UserProfilePicture;
import com.skillvault.backend.Repositories.UserProfilePictureRepository;
import com.skillvault.backend.Repositories.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

@Slf4j
@Service
public class AzureService {
    private final BlobContainerClient containerClient;

    private final UserProfilePictureRepository profilePictureRepository;
    private final UserRepository userRepository;

    public AzureService(
            UserProfilePictureRepository profilePictureRepository,
            UserRepository userRepository,
            @Value("${azure.storage.connection-string}") String connectionString,
            @Value("${azure.storage.container-name}") String containerName){

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString).buildClient();

        this.profilePictureRepository = profilePictureRepository;
        this.userRepository = userRepository;

        containerClient = serviceClient.getBlobContainerClient(containerName);

        if (!containerClient.exists()){
            containerClient.create();
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
            getBlobClient(profilePicture).upload(new ByteArrayInputStream(data), data.length, true);
            

            profilePicture.setBlobId(blobId);
            user.setProfilePicture(profilePicture);
            userRepository.save(user);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error on file reading.");
        }
    }


    public UserProfilePicture preparePicture(MultipartFile file, User user){
        UserProfilePicture picture = new UserProfilePicture();
        picture.setUser(user);
        picture.setBlobId(UUID.randomUUID() + file.getOriginalFilename());

        return profilePictureRepository.findByUser_Id(user.getId()).orElse(picture);
    }

    private BlobClient getBlobClient(UserProfilePicture document){
        String uniqueBlobName = document.getBlobId();
        return containerClient.getBlobClient(uniqueBlobName);
    }}
