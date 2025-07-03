package com.hivcare.controller;

import com.hivcare.dto.response.ApiResponse;
import com.hivcare.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FileController {

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @PostMapping("/upload/test-result")
    @PreAuthorize("hasRole('STAFF') or hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> uploadTestResultFile(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileUploadUtil.uploadTestResultFile(file);
            
            Map<String, String> response = new HashMap<>();
            response.put("filePath", filePath);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", String.valueOf(file.getSize()));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi upload file: " + e.getMessage()));
        }
    }

    @PostMapping("/upload/profile-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileUploadUtil.uploadProfileImage(file);
            
            Map<String, String> response = new HashMap<>();
            response.put("filePath", filePath);
            response.put("fileName", file.getOriginalFilename());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi upload ảnh: " + e.getMessage()));
        }
    }

    @PostMapping("/upload/medical-document")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<?> uploadMedicalDocument(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileUploadUtil.uploadMedicalDocument(file);
            
            Map<String, String> response = new HashMap<>();
            response.put("filePath", filePath);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", String.valueOf(file.getSize()));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi upload tài liệu: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{subDirectory}/{fileName:.+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String subDirectory,
            @PathVariable String fileName) {
        try {
            String filePath = subDirectory + "/" + fileName;
            
            if (!fileUploadUtil.fileExists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            Path path = Paths.get("./uploads").resolve(filePath);
            Resource resource = new UrlResource(path.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{subDirectory}/{fileName:.+}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> deleteFile(
            @PathVariable String subDirectory,
            @PathVariable String fileName) {
        try {
            String filePath = subDirectory + "/" + fileName;
            fileUploadUtil.deleteFile(filePath);
            
            return ResponseEntity.ok(new ApiResponse(true, "Xóa file thành công"));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi xóa file: " + e.getMessage()));
        }
    }

    @GetMapping("/check/{subDirectory}/{fileName:.+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> checkFileExists(
            @PathVariable String subDirectory,
            @PathVariable String fileName) {
        
        String filePath = subDirectory + "/" + fileName;
        boolean exists = fileUploadUtil.fileExists(filePath);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }
}
