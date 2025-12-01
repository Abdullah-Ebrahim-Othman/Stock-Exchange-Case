package com.example.stockexchange.exception;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException() {
        this("Resource Not Found!");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
