package com.hivcare.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * API Service for integrated frontend-backend system
 * Simplified version without WebClient since frontend is part of backend
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
     * Simple health check method
     */
    public boolean isHealthy() {
        return true;
    }

    /**
     * Get backend status
     */
    public String getBackendStatus() {
        return "Online - Integrated Mode";
    }

    /**
     * Get API base URL
     */
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    /**
     * Get configured timeout
     */
    public int getTimeout() {
        return timeout;
    }

    // Mock methods for compatibility with existing code
    public ApiResponse<String> healthCheck() {
        return new ApiResponse<>(true, "Health check successful", "System is running");
    }

    public ApiResponse<String> getPublicDoctors() {
        return new ApiResponse<>(true, "Success", "[]"); // Mock empty doctors list
    }

    public ApiResponse<String> getArvProtocols() {
        return new ApiResponse<>(true, "Success", "[]"); // Mock empty protocols list
    }

    // Simple mock methods that don't require WebClient
    public ApiResponse<Map<String, Object>> login(String username, String password) {
        // In integrated mode, login is handled directly by HomeWebController
        return new ApiResponse<>(false, "Use direct login endpoint", null);
    }

    public ApiResponse<Map<String, Object>> register(Map<String, Object> registerData) {
        // In integrated mode, registration is handled directly by HomeWebController
        return new ApiResponse<>(false, "Use direct registration endpoint", null);
    }

    public ApiResponse<String> get(String endpoint, String token) {
        // Mock GET response
        return new ApiResponse<>(true, "Mock response", "{}");
    }

    public ApiResponse<String> post(String endpoint, Object data, String token) {
        // Mock POST response
        return new ApiResponse<>(true, "Mock response", "{}");
    }

    public ApiResponse<String> put(String endpoint, Object data, String token) {
        // Mock PUT response
        return new ApiResponse<>(true, "Mock response", "{}");
    }

    // Inner class for API response
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