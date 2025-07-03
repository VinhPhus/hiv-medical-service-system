package com.hivcare.controller;

import com.hivcare.dto.response.ApiResponse;
import com.hivcare.entity.User;
import com.hivcare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        return userService.getUserByUsername(username)
                .map(user -> ResponseEntity.ok().body(user))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateCurrentUserProfile(@RequestBody User userDetails) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            User currentUser = userService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            User updatedUser = userService.updateUser(currentUser.getId(), userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi cập nhật hồ sơ: " + e.getMessage()));
        }
    }

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(
            @RequestParam String oldPassword, 
            @RequestParam String newPassword) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            User currentUser = userService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            boolean success = userService.changePassword(currentUser.getId(), oldPassword, newPassword);
            
            if (success) {
                return ResponseEntity.ok(new ApiResponse(true, "Đổi mật khẩu thành công"));
            } else {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Mật khẩu cũ không đúng"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi đổi mật khẩu: " + e.getMessage()));
        }
    }

    @PostMapping("/create-staff")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> createStaffUser(@RequestBody User user) {
        try {
            if (user.getRole() != User.Role.STAFF && user.getRole() != User.Role.DOCTOR) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Chỉ có thể tạo tài khoản STAFF hoặc DOCTOR"));
            }

            if (userService.existsByUsername(user.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Tên đăng nhập đã tồn tại"));
            }

            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Email đã tồn tại"));
            }

            User savedUser = userService.createUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi tạo tài khoản nhân viên: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok().body(user))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi cập nhật tài khoản: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse(true, "Xóa tài khoản thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi xóa tài khoản: " + e.getMessage()));
        }
    }
}
