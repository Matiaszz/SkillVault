package com.skillvault.backend.Utils;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

public class FileUtils {

    public static boolean validateProfileImageExtension(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;
        String[] imgExtensions = {".jpg", ".jpeg", ".png", ".webp"};

        String fileName = file.getOriginalFilename();

        if (fileName == null) return false;

        return Arrays.stream(imgExtensions).anyMatch(fileName.toLowerCase()::endsWith);
    }

    public static boolean validateCertificateExtension(MultipartFile file){
        if (file == null || file.isEmpty()) return false;
        String[] extensions = {".jpg", ".jpeg", ".png", ".webp", ".pdf", ".docx"};

        String fileName = file.getOriginalFilename();

        if (fileName == null) return false;

        return Arrays.stream(extensions).anyMatch(fileName.toLowerCase()::endsWith);
    }
}
