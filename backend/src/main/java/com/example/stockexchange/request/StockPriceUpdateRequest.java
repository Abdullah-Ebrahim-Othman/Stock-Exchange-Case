package com.example.stockexchange.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockPriceUpdateRequest {

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal currentPrice;
}
