package com.example.finalexam_jvnc.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads/products}")
    private String uploadDir;

    /**
     * Store uploaded file and return the file path
     */
    public String storeFile(MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Không thể upload file rỗng");
            }

            // Validate file type (only images)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Chỉ chấp nhận file hình ảnh (jpg, png, gif, etc.)");
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Save file
            Path targetLocation = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path for storage in database
            return "/uploads/products/" + uniqueFilename;

        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu file. Vui lòng thử lại!", ex);
        }
    }

    /**
     * Delete file from storage
     */
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && !fileUrl.isEmpty()) {
                // Extract filename from URL
                String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get(uploadDir).resolve(filename);

                // Delete file if it exists
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ex) {
            // Log error but don't throw exception
            System.err.println("Could not delete file: " + fileUrl + ". Error: " + ex.getMessage());
        }
    }

    /**
     * Validate if file is an image
     */
    public boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * Get maximum file size (5MB)
     */
    public long getMaxFileSize() {
        return 5 * 1024 * 1024; // 5MB in bytes
    }
}
