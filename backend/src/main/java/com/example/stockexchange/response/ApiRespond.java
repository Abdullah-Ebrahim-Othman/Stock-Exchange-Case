package com.example.stockexchange.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@Data
public class ApiRespond<T> {
    private int status;
    private String message;
    private T data;
    private long timestamp;

    public ApiRespond(HttpStatus status, String message, T data) {
        this.status = status.value();
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
}
