package com.hivcare.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class FileUploadUtil {

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.file.max-file-size:10MB}")
    private String maxFileSize;

    private final List<String> allowedImageTypes = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif"
    );

    private final List<String> allowedDocumentTypes = Arrays.asList(
        "application/pdf", "application/msword", 
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    public String uploadFile(MultipartFile file, String subDirectory) throws IOException {
        validateFile(file);
        
        String fileName = generateFileName(file.getOriginalFilename());
        Path uploadPath = Paths.get(uploadDir, subDirectory);
        
        // Create directory if it doesn't exist
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return subDirectory + "/" + fileName;
    }

    public String uploadTestResultFile(MultipartFile file) throws IOException {
        return uploadFile(file, "test-results");
    }

    public String uploadProfileImage(MultipartFile file) throws IOException {
        if (!allowedImageTypes.contains(file.getContentType())) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh (JPEG, PNG, GIF)");
        }
        return uploadFile(file, "profiles");
    }

    public String uploadMedicalDocument(MultipartFile file) throws IOException {
        if (!allowedDocumentTypes.contains(file.getContentType()) && 
            !allowedImageTypes.contains(file.getContentType())) {
            throw new IllegalArgumentException("Chỉ chấp nhận file PDF, Word hoặc ảnh");
        }
        return uploadFile(file, "medical-documents");
    }

    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(uploadDir, filePath);
        Files.deleteIfExists(path);
    }

    public boolean fileExists(String filePath) {
        Path path = Paths.get(uploadDir, filePath);
        return Files.exists(path);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        if (file.getSize() > getMaxFileSizeInBytes()) {
            throw new IllegalArgumentException("File quá lớn. Kích thước tối đa: " + maxFileSize);
        }
    }

    private String generateFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + extension;
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.lastIndexOf(".") > 0) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }

    private long getMaxFileSizeInBytes() {
        // Convert string like "10MB" to bytes
        String size = maxFileSize.toUpperCase();
        if (size.endsWith("MB")) {
            return Long.parseLong(size.replace("MB", "")) * 1024 * 1024;
        } else if (size.endsWith("KB")) {
            return Long.parseLong(size.replace("KB", "")) * 1024;
        }
        return 10 * 1024 * 1024; // Default 10MB
    }
}
