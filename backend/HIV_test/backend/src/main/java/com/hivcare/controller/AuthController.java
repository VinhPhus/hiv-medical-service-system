package com.hivcare.controller;

import com.hivcare.dto.request.LoginRequest;
import com.hivcare.dto.request.SignupRequest;
import com.hivcare.dto.response.ApiResponse;
import com.hivcare.dto.response.JwtResponse;
import com.hivcare.entity.User;
import com.hivcare.entity.Patient;
import com.hivcare.repository.UserRepository;
import com.hivcare.repository.PatientRepository;
import com.hivcare.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.debug("Đang xử lý đăng nhập cho user: {}", loginRequest.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(loginRequest.getUsername());
            
            User userDetails = (User) authentication.getPrincipal();
            
            // Update last login
            userDetails.setLastLogin(LocalDateTime.now());
            userRepository.save(userDetails);

            logger.debug("Đăng nhập thành công cho user: {}, role: {}", userDetails.getUsername(), userDetails.getRole());

            return ResponseEntity.ok(new JwtResponse(jwt,
                                                    refreshToken,
                                                    userDetails.getId(), 
                                                    userDetails.getUsername(), 
                                                    userDetails.getEmail(),
                                                    userDetails.getFullName(),
                                                    "ROLE_" + userDetails.getRole().name()));
        } catch (Exception e) {
            logger.error("Lỗi khi đăng nhập: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi đăng nhập: " + e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Tên đăng nhập đã được sử dụng!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Email đã được sử dụng!"));
        }

        // Create new user account
        User user = new User(signUpRequest.getUsername(), 
                           signUpRequest.getEmail(),
                           encoder.encode(signUpRequest.getPassword()),
                           signUpRequest.getFullName(),
                           User.Role.PATIENT);

        user.setPhoneNumber(signUpRequest.getPhoneNumber());
        user.setAnonymous(signUpRequest.isAnonymous());
        
        User savedUser = userRepository.save(user);

        // Create patient profile for patient
        if (user.getRole() == User.Role.PATIENT) {
            Patient patient = new Patient(savedUser);
            patientRepository.save(patient);
        }

        return ResponseEntity.ok(new ApiResponse(true, "Đăng ký tài khoản thành công!"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestParam String refreshToken) {
        try {
            if (jwtUtils.validateJwtToken(refreshToken)) {
                String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
                String newAccessToken = jwtUtils.generateTokenFromUsername(username);
                
                return ResponseEntity.ok(new JwtResponse(newAccessToken, refreshToken, null, username, null, null, null));
            } else {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Refresh token không hợp lệ!"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi refresh token: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        return ResponseEntity.ok(new ApiResponse(true, "Đăng xuất thành công!"));
    }
}
