package com.example.stockexchange.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddStocksToExchangeRequest {

    @NotNull(message = "Stock IDs cannot be null")
    @NotEmpty(message = "At least one stock ID must be provided")
    // want to add positive
    private List<Long> stockIds;

}
