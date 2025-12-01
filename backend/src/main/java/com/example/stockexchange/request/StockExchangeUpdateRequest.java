package com.example.stockexchange.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockExchangeUpdateRequest {

    @Size(min = 3, max = 30, message = "Name must be at least 3 characters long")
    private String name;

    @Size(min = 3, max = 255, message = "description must be at least 3 characters long")
    private String description;
}
