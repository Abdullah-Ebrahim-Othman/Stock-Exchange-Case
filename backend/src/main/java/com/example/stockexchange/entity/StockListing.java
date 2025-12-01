package com.example.stockexchange.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@Entity(name = "stock_listing")
@Data
@NoArgsConstructor
@Table(name = "stock_exchange_stock")
public class StockListing {

    @EmbeddedId
    private StockListingId stockListingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("stockExchangeId")
    @JoinColumn(name = "stock_exchange_id")
    private StockExchange stockExchange;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("stockId")
    @JoinColumn(name = "stock_id")
    private Stock stock;

    public StockListing(StockExchange stockExchange, Stock stock) {
        this.stockExchange = stockExchange;
        this.stock = stock;
        this.stockListingId = new StockListingId(stockExchange.getStockExchangeId(), stock.getStockId());
    }
}
