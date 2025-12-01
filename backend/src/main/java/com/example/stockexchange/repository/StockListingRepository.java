package com.example.stockexchange.repository;

import com.example.stockexchange.entity.Stock;
import com.example.stockexchange.entity.StockExchange;
import com.example.stockexchange.entity.StockListing;
import com.example.stockexchange.entity.StockListingId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface StockListingRepository extends JpaRepository<StockListing, StockListingId> {

    @Query("SELECT COUNT(sl) FROM stock_listing sl WHERE sl.stockExchange.stockExchangeId = :id")
    long countByStockExchangeId(@Param("id") Long stockExchangeId);

    @Query("SELECT sl.stock FROM stock_listing sl WHERE sl.stockExchange.stockExchangeId = :id")
    Page<Stock> findStocksByStockExchangeId(@Param("id") Long stockExchangeId, Pageable pageable);

    @Query("SELECT sl.stockExchange FROM stock_listing sl WHERE sl.stock.stockId = :id")
    Page<StockExchange> findStockExchangesByStockId(@Param("id") Long stockId, Pageable pageable);
    
    @Query("SELECT s FROM Stock s WHERE s NOT IN " +
           "(SELECT sl.stock FROM stock_listing sl WHERE sl.stockExchange.stockExchangeId = :exchangeId)")
    Page<Stock> findStocksNotInExchange(@Param("exchangeId") Long exchangeId, Pageable pageable);

    @Query("SELECT sl.stockListingId FROM stock_listing sl " +
           "WHERE sl.stockExchange.stockExchangeId = :stockExchangeId AND sl.stock.stockId IN :stockIds")
    List<StockListingId> findExistingListings(
            @Param("stockExchangeId") Long stockExchangeId,
            @Param("stockIds") List<Long> stockIds
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM stock_listing sl WHERE sl.stockExchange.stockExchangeId = :stockExchangeId")
    void deleteByStockExchangeId(@Param("stockExchangeId") Long stockExchangeId);

}
