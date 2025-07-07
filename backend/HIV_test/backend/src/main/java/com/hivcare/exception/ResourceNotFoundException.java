package com.hivcare.exception;

public class ResourceNotFoundException extends RuntimeException {
    
  
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Không tìm thấy %s với %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
