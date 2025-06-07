package com.hivcare.dto.response;

import com.hivcare.enums.UserRole;
import com.hivcare.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private UserRole role;
    private UserStatus status;
    private String avatar;
    private LocalDateTime dateOfBirth;
    private String address;
    private String emergencyContact;
    private LocalDateTime lastLogin;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}