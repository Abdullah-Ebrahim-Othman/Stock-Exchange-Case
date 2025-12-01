package com.example.stockexchange.exception;

public class DuplicateResourceException extends RuntimeException{
    public DuplicateResourceException() {
        this("Already Exists!");
    }

    public DuplicateResourceException(String message) {
        super(message);
    }
}
