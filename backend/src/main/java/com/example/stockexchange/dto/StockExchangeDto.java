package com.example.stockexchange.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockExchangeDto {

    private Long stockExchangeId;

    private String name;

    private String description;

    private boolean liveInMarket;
}
