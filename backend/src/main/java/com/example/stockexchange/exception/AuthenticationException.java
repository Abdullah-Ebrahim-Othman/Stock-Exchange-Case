package com.example.stockexchange.exception;

public class AuthenticationException extends RuntimeException{
    public AuthenticationException() {
        this("Not Authorized");
    }

    public AuthenticationException(String message) {
        super(message);
    }
}
