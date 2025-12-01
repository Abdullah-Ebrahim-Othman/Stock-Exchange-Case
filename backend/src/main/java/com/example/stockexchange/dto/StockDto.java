package com.example.stockexchange.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockDto {

    private Long stockId;

    private String name;

    private String description;

    private BigDecimal currentPrice;

    private LocalDateTime updatedAt;
}
