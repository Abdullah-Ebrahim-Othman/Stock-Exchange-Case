package com.example.stockexchange.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockListingDto {

    private StockExchangeDto stockExchangeDto;
    private StockDto stockDto;
}
