package com.example.stockexchange.response;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data

public class CreateStockResponse {
    private long stockId;

    private String name;

    private String description;

    private double currentPrice;

    private LocalDateTime updatedAt;

    private int version;

    public CreateStockResponse(long stockId, String name, String description, double currentPrice, int version) {
        this.stockId = stockId;
        this.name = name;
        this.description = description;
        this.currentPrice = currentPrice;
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
        this.version = version;
    }
}
