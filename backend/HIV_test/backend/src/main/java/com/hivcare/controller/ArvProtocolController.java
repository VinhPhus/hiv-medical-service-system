package com.hivcare.controller;

import com.hivcare.dto.response.ApiResponse;
import com.hivcare.entity.ArvProtocol;
import com.hivcare.service.ArvProtocolService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/arv-protocols")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ArvProtocolController {

    private static final Logger logger = LoggerFactory.getLogger(ArvProtocolController.class);

    @Autowired
    private ArvProtocolService arvProtocolService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DOCTOR') or hasRole('STAFF')")
    public ResponseEntity<Page<ArvProtocol>> getAllProtocols(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search) {
        try {
            logger.debug("Fetching protocols with page: {}, size: {}, sortBy: {}, sortDir: {}, search: {}", 
                page, size, sortBy, sortDir, search);
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ArvProtocol> protocols = arvProtocolService.getAllProtocols(search, pageable);
            
            logger.debug("Found {} protocols", protocols.getTotalElements());
            return ResponseEntity.ok(protocols);
        } catch (Exception e) {
            logger.error("Error fetching protocols", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/public")
    public ResponseEntity<List<ArvProtocol>> getActiveProtocols() {
        try {
            List<ArvProtocol> activeProtocols = arvProtocolService.getActiveProtocols();
            return ResponseEntity.ok(activeProtocols);
        } catch (Exception e) {
            logger.error("Error fetching active protocols", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-protocols")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DOCTOR') or hasRole('STAFF')")
    public ResponseEntity<List<ArvProtocol>> getMyProtocols() {
        try {
            // TODO: Lấy danh sách phác đồ của người dùng hiện tại
            // Hiện tại trả về tất cả phác đồ (bạn có thể sửa lại logic này nếu cần)
            List<ArvProtocol> protocols = arvProtocolService.getAllProtocols();
            return ResponseEntity.ok(protocols);
        } catch (Exception e) {
            logger.error("Error fetching my protocols", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DOCTOR') or hasRole('STAFF')")
    public ResponseEntity<ArvProtocol> getProtocolById(@PathVariable Long id) {
        try {
            return arvProtocolService.getProtocolById(id)
                    .map(protocol -> ResponseEntity.ok().body(protocol))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching protocol by ID: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DOCTOR') or hasRole('STAFF')")
    public ResponseEntity<ArvProtocol> getProtocolByCode(@PathVariable String code) {
        try {
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            return arvProtocolService.getProtocolByCode(code)
                    .map(protocol -> ResponseEntity.ok().body(protocol))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching protocol by code: {}", code, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<ArvProtocol>> getProtocolsByCategory(@PathVariable ArvProtocol.PatientCategory category) {
        try {
            if (category == null) {
                return ResponseEntity.badRequest().build();
            }
            List<ArvProtocol> protocols = arvProtocolService.getProtocolsByCategory(category);
            return ResponseEntity.ok(protocols);
        } catch (Exception e) {
            logger.error("Error fetching protocols by category: {}", category, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/recommended/{category}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<ArvProtocol>> getRecommendedProtocols(@PathVariable ArvProtocol.PatientCategory category) {
        try {
            if (category == null) {
                return ResponseEntity.badRequest().build();
            }
            List<ArvProtocol> protocols = arvProtocolService.getRecommendedProtocols(category);
            return ResponseEntity.ok(protocols);
        } catch (Exception e) {
            logger.error("Error fetching recommended protocols for category: {}", category, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<List<ArvProtocol>> searchProtocols(@RequestParam String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            List<ArvProtocol> protocols = arvProtocolService.searchProtocols(searchTerm);
            return ResponseEntity.ok(protocols);
        } catch (Exception e) {
            logger.error("Error searching protocols with term: {}", searchTerm, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> createProtocol(@Valid @RequestBody ArvProtocol protocol,
                                          BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Dữ liệu phác đồ không hợp lệ"));
            }

            // Validate required fields
            if (protocol.getName() == null || protocol.getName().trim().isEmpty() ||
                protocol.getCode() == null || protocol.getCode().trim().isEmpty() ||
                protocol.getPatientCategory() == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Các trường bắt buộc không được để trống"));
            }

            ArvProtocol savedProtocol = arvProtocolService.createProtocol(protocol);
            logger.info("Created new protocol with ID: {}", savedProtocol.getId());
            return ResponseEntity.ok(savedProtocol);
        } catch (Exception e) {
            logger.error("Error creating protocol", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi tạo phác đồ ARV: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> updateProtocol(@PathVariable Long id, 
                                          @Valid @RequestBody ArvProtocol protocolDetails,
                                          BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Dữ liệu cập nhật không hợp lệ"));
            }

            // Validate required fields
            if (protocolDetails.getName() == null || protocolDetails.getName().trim().isEmpty() ||
                protocolDetails.getCode() == null || protocolDetails.getCode().trim().isEmpty() ||
                protocolDetails.getPatientCategory() == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Các trường bắt buộc không được để trống"));
            }

            ArvProtocol updatedProtocol = arvProtocolService.updateProtocol(id, protocolDetails);
            logger.info("Updated protocol with ID: {}", id);
            return ResponseEntity.ok(updatedProtocol);
        } catch (Exception e) {
            logger.error("Error updating protocol with ID: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi cập nhật phác đồ ARV: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> activateProtocol(@PathVariable Long id) {
        try {
            ArvProtocol activatedProtocol = arvProtocolService.activateProtocol(id);
            logger.info("Activated protocol with ID: {}", id);
            return ResponseEntity.ok(activatedProtocol);
        } catch (Exception e) {
            logger.error("Error activating protocol with ID: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi kích hoạt phác đồ ARV: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> deactivateProtocol(@PathVariable Long id) {
        try {
            ArvProtocol deactivatedProtocol = arvProtocolService.deactivateProtocol(id);
            logger.info("Deactivated protocol with ID: {}", id);
            return ResponseEntity.ok(deactivatedProtocol);
        } catch (Exception e) {
            logger.error("Error deactivating protocol with ID: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi vô hiệu hóa phác đồ ARV: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/clone")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> cloneProtocol(
            @PathVariable Long id, 
            @RequestParam String newCode, 
            @RequestParam String newName) {
        try {
            if (newCode == null || newCode.trim().isEmpty() ||
                newName == null || newName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Mã và tên phác đồ mới không được để trống"));
            }

            ArvProtocol clonedProtocol = arvProtocolService.cloneProtocol(id, newCode, newName);
            logger.info("Cloned protocol {} to new protocol with ID: {}", id, clonedProtocol.getId());
            return ResponseEntity.ok(clonedProtocol);
        } catch (Exception e) {
            logger.error("Error cloning protocol with ID: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi sao chép phác đồ ARV: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProtocol(@PathVariable Long id) {
        try {
            arvProtocolService.deleteProtocol(id);
            logger.info("Deleted protocol with ID: {}", id);
            return ResponseEntity.ok(new ApiResponse(true, "Xóa phác đồ ARV thành công"));
        } catch (Exception e) {
            logger.error("Error deleting protocol with ID: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi xóa phác đồ ARV: " + e.getMessage()));
        }
    }
}
