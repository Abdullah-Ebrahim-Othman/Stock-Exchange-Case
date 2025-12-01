package com.example.stockexchange.service;

import com.example.stockexchange.dto.StockDto;
import com.example.stockexchange.dto.StockExchangeDto;
import com.example.stockexchange.dto.StockListingDto;
import com.example.stockexchange.entity.Stock;
import com.example.stockexchange.entity.StockExchange;
import com.example.stockexchange.entity.StockListing;
import com.example.stockexchange.entity.StockListingId;
import com.example.stockexchange.exception.DuplicateResourceException;
import com.example.stockexchange.exception.ResourceNotFoundException;
import com.example.stockexchange.mapper.StockExchangeMapper;
import com.example.stockexchange.mapper.StockMapper;
import com.example.stockexchange.repository.StockExchangeRepository;
import com.example.stockexchange.repository.StockListingRepository;
import com.example.stockexchange.repository.StockRepository;
import com.example.stockexchange.request.StockExchangeCreationRequest;
import com.example.stockexchange.request.StockExchangeUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class StockExchangeService {

    private final StockExchangeRepository stockExchangeRepository;
    private final StockRepository stockRepository;
    private final StockListingRepository stockListingRepository;
    private final StockExchangeMapper stockExchangeMapper;
    private final StockMapper stockMapper;

    public Page<StockExchangeDto> getAllStockExchanges(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StockExchange> stockExchangePage = stockExchangeRepository.findAll(pageable);
        return stockExchangePage.map(stockExchangeMapper::map);
    }

    public Page<StockExchangeDto> getAllStockExchangesLiveInMarket(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StockExchange> stockExchangePage = stockExchangeRepository.findByLiveInMarketTrue(pageable);
        return stockExchangePage.map(stockExchangeMapper::map);
    }
    
    @Transactional(readOnly = true)
    public Page<StockDto> findStocksNotInExchange(Long exchangeId, int page, int size) {
        // Verify the stock exchange exists
        if (!stockExchangeRepository.existsById(exchangeId)) {
            throw new ResourceNotFoundException("Stock exchange not found with id: " + exchangeId);
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Stock> stocks = stockListingRepository.findStocksNotInExchange(exchangeId, pageable);
        return stocks.map(stockMapper::map);
    }

    public StockExchangeDto getStockExchangeById(Long id) {
        return stockExchangeRepository.findById(id)
                .map(stockExchangeMapper::map)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Exchange not found with id: " + id));
    }

    private Long getNumberOfStocks(long stockExchangeId) {
        return stockListingRepository.countByStockExchangeId(stockExchangeId);
    }

    public StockExchangeDto createStockExchange(StockExchangeCreationRequest stockExchangeCreationRequest) {
        StockExchange stockExchange = stockExchangeMapper.map(stockExchangeCreationRequest);
        stockExchangeRepository.save(stockExchange);
        return stockExchangeMapper.map(stockExchange);
    }

    @Transactional
    public StockExchangeDto updateStockExchange(Long stockExchangeId, StockExchangeUpdateRequest stockExchangeUpdateRequest) {
        StockExchange stockExchange = stockExchangeRepository.findById(stockExchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Exchange not found with id: " + stockExchangeId));

        stockExchangeMapper.map(stockExchangeUpdateRequest, stockExchange);
        StockExchange updatedStockExchange = stockExchangeRepository.save(stockExchange);
        return stockExchangeMapper.map(updatedStockExchange);

    }
    @Transactional
    public void deleteStockExchange(Long stockExchangeId) {
        StockExchange stockExchange = stockExchangeRepository.findById(stockExchangeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock Exchange not found with id: " + stockExchangeId));

        stockExchangeRepository.delete(stockExchange);
        // StockListings are automatically deleted due to cascade
        // Stocks remain untouched
    }

    public Page<StockDto> getAllStocksByExchange(Long stockExchangeId, int page, int size, String sortBy) {
        if (!stockExchangeRepository.existsById(stockExchangeId)) {
            throw new ResourceNotFoundException("Stock Exchange not found with id: " + stockExchangeId);
        }

        // Map the sort field to use the correct entity field name
        String sortField = "name".equals(sortBy) ? "stock.name" : sortBy;
//        Pageable pageable = PageRequest.of(page, size, Sort.by(sortField).ascending());
        Pageable pageable = PageRequest.of(page, size);
        Page<Stock> stockPage = stockListingRepository.findStocksByStockExchangeId(stockExchangeId, pageable);
        return stockPage.map(stockMapper::map);
    }

    @Transactional
    public StockListingDto addStockToStockExchange(Long stockExchangeId, Long stockId) {
        StockExchange stockExchange = stockExchangeRepository.findById(stockExchangeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock Exchange not found with id: " + stockExchangeId));

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock not found with id: " + stockId));

        return addStockToExchange(stockExchange, stock);
    }

    @Transactional
    public List<StockListingDto> addStocksToStockExchange(Long stockExchangeId, List<Long> stockIds) {
        if (stockIds == null || stockIds.isEmpty()) {
            throw new IllegalArgumentException("Stock IDs list cannot be null or empty");
        }

        // Get the stock exchange once
        StockExchange stockExchange = stockExchangeRepository.findById(stockExchangeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock Exchange not found with id: " + stockExchangeId));

        // Get all stocks at once
        List<Stock> stocks = stockRepository.findAllById(stockIds);
        if (stocks.size() != stockIds.size()) {
            Set<Long> foundIds = stocks.stream()
                    .map(Stock::getStockId)
                    .collect(Collectors.toSet());
            
            List<Long> missingIds = stockIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
                    
            throw new ResourceNotFoundException("Stocks not found with ids: " + missingIds);
        }

        // Check for existing listings
        List<StockListingId> existingListings = stockListingRepository.findExistingListings(
                stockExchangeId, 
                stockIds
        );
        
        if (!existingListings.isEmpty()) {
            throw new DuplicateResourceException(
                    "Some stocks are already listed on this exchange: " + 
                    existingListings.stream()
                            .map(StockListingId::getStockId)
                            .map(String::valueOf)
                            .collect(Collectors.joining(", ")));
        }

        // Create and save all new listings
        List<StockListing> listings = stocks.stream()
                .map(stock -> new StockListing(stockExchange, stock))
                .collect(Collectors.toList());
        
        stockListingRepository.saveAll(listings);
        updateLiveMarketStatus(stockExchange);

        // Convert to DTOs
        return listings.stream()
                .map(listing -> new StockListingDto(
                        stockExchangeMapper.map(stockExchange), 
                        stockMapper.map(listing.getStock())))
                .collect(Collectors.toList());
    }
    
    private StockListingDto addStockToExchange(StockExchange stockExchange, Stock stock) {
        StockListingId listingId = new StockListingId(stockExchange.getStockExchangeId(), stock.getStockId());
        if (stockListingRepository.existsById(listingId)) {
            throw new DuplicateResourceException(
                    "Stock with id " + stock.getStockId() + " is already listed on Stock Exchange with id " + stockExchange.getStockExchangeId());
        }

        StockListing stockListing = new StockListing(stockExchange, stock);
        stockListingRepository.save(stockListing);

        updateLiveMarketStatus(stockExchange);

        return new StockListingDto(stockExchangeMapper.map(stockExchange), stockMapper.map(stock));
    }


    @Transactional
    public void removeStocksFromStockExchange(Long stockExchangeId, List<Long> stockIds) {
        if (stockIds == null || stockIds.isEmpty()) {
            throw new IllegalArgumentException("Stock IDs list cannot be null or empty");
        }

        // Get the stock exchange once
        StockExchange stockExchange = stockExchangeRepository.findById(stockExchangeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock Exchange not found with id: " + stockExchangeId));

        // Get all stock listings at once
        List<StockListingId> listingIds = stockIds.stream()
                .map(stockId -> new StockListingId(stockExchangeId, stockId))
                .toList();
                
        List<StockListing> listings = stockListingRepository.findAllById(listingIds);
        
        // Check if all stocks were found in the exchange
        if (listings.size() != stockIds.size()) {
            Set<Long> foundStockIds = listings.stream()
                    .map(listing -> listing.getStock().getStockId())
                    .collect(Collectors.toSet());
                    
            List<Long> missingStockIds = stockIds.stream()
                    .filter(id -> !foundStockIds.contains(id))
                    .toList();
                    
            throw new ResourceNotFoundException(
                    "The following stocks are not listed on this exchange: " + missingStockIds);
        }

        // Delete all listings in batch
        stockListingRepository.deleteAllInBatch(listings);
        updateLiveMarketStatus(stockExchange);
    }

    @Transactional
    public void removeStockFromStockExchange(Long stockExchangeId, Long stockId) {
        StockExchange stockExchange = stockExchangeRepository.findById(stockExchangeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock Exchange not found with id: " + stockExchangeId));

        StockListingId listingId = new StockListingId(stockExchangeId, stockId);
        StockListing stockListing = stockListingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock with id " + stockId + " is not listed on this Stock Exchange"));

        stockListingRepository.delete(stockListing);
        updateLiveMarketStatus(stockExchange);
    }

    public void updateLiveMarketStatus(StockExchange stockExchange) {
        long remainingStocks = getNumberOfStocks(stockExchange.getStockExchangeId());
        boolean shouldBeLive = remainingStocks >= 10;

        if (stockExchange.isLiveInMarket() != shouldBeLive) {
            stockExchange.setLiveInMarket(shouldBeLive);
            // No need to call save() if using @Transactional - changes are auto-detected
        }
    }
}
