package com.example.stockexchange.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;


// composite key
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockListingId implements Serializable {

    @Column(name = "stock_exchange_id")
    private Long stockExchangeId;

    @Column(name = "stock_id")
    private Long stockId;
}
