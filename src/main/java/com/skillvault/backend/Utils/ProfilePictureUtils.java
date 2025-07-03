package com.skillvault.backend.Utils;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

public class ProfilePictureUtils {

    public static boolean validateProfileImageExtension(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;
        String[] imgExtensions = {".jpg", ".jpeg", ".png", ".webp"};

        String fileName = file.getOriginalFilename();

        if (fileName == null) return false;

        return Arrays.stream(imgExtensions).anyMatch(fileName.toLowerCase()::endsWith);
    }
}
