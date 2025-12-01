package com.example.stockexchange.repository;

import com.example.stockexchange.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    boolean existsByName(String stockName);
}
