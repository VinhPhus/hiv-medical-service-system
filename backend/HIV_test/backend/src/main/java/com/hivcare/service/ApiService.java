package com.hivcare.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Dịch vụ API cho hệ thống tích hợp frontend và backend
 * Phiên bản đơn giản hóa, không sử dụng WebClient vì frontend đã nằm trong backend
 */
@Service
public class ApiService {

    private final ObjectMapper objectMapper;

    @Value("${api.base-url:http://localhost:8080/api}")
    private String apiBaseUrl;

    @Value("${api.timeout:30000}")
    private int timeout;

    public ApiService() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Hàm kiểm tra sức khỏe hệ thống đơn giản
     */
    public boolean isHealthy() {
        return true;
    }

    /**
     * Lấy trạng thái của backend
     */
    public String getBackendStatus() {
        return "Online - Integrated Mode";
    }

    /**
     * Lấy địa chỉ gốc (base URL) của API
     */
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }
    
    /**
     * Lấy thời gian timeout đã được cấu hình
     */
    public int getTimeout() {
        return timeout;
    }

    // Các hàm giả lập để tương thích với mã nguồn hiện tại
    public ApiResponse<String> healthCheck() {
        return new ApiResponse<>(true, "Health check successful", "System is running");
    }

    public ApiResponse<String> getPublicDoctors() {
        return new ApiResponse<>(true, "Success", "[]"); // Mock empty doctors list
    }

    public ApiResponse<String> getArvProtocols() {
        return new ApiResponse<>(true, "Success", "[]"); // Mock empty protocols list
    }

    // Các hàm giả lập đơn giản không cần WebClient
    public ApiResponse<Map<String, Object>> login(String username, String password) {
        // Ở chế độ tích hợp, đăng nhập được xử lý trực tiếp bởi HomeWebController
        return new ApiResponse<>(false, "Use direct login endpoint", null);
    }

    public ApiResponse<Map<String, Object>> register(Map<String, Object> registerData) {
        // Ở chế độ tích hợp, đăng ký được xử lý trực tiếp bởi HomeWebController
        return new ApiResponse<>(false, "Use direct registration endpoint", null);
    }

    public ApiResponse<String> get(String endpoint, String token) {
        // Phản hồi GET giả lập
        return new ApiResponse<>(true, "Mock response", "{}");
    }

    public ApiResponse<String> post(String endpoint, Object data, String token) {
        // Phản hồi POST giả lập
        return new ApiResponse<>(true, "Mock response", "{}");
    }

    public ApiResponse<String> put(String endpoint, Object data, String token) {
        // Phản hồi PUT giả lập
        return new ApiResponse<>(true, "Mock response", "{}");
    }

    // Lớp nội bộ cho phản hồi API
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
    }
}