package com.example.stockexchange.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "stock_exchange")
public class StockExchange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_exchange_id")
    private Long stockExchangeId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "live_in_market")
    private volatile boolean liveInMarket;

    @OneToMany(mappedBy = "stockExchange",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<StockListing> stockListings = new ArrayList<>();

    @Version
    private int version;
}
