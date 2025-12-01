package com.example.stockexchange.service;

import com.example.stockexchange.dto.StockDto;
import com.example.stockexchange.dto.StockExchangeDto;
import com.example.stockexchange.entity.Stock;
import com.example.stockexchange.entity.StockExchange;
import com.example.stockexchange.entity.StockListing;
import com.example.stockexchange.exception.DuplicateResourceException;
import com.example.stockexchange.exception.ResourceNotFoundException;
import com.example.stockexchange.mapper.StockExchangeMapper;
import com.example.stockexchange.mapper.StockMapper;
import com.example.stockexchange.repository.StockListingRepository;
import com.example.stockexchange.repository.StockRepository;
import com.example.stockexchange.request.StockCreationRequest;
import com.example.stockexchange.request.StockPriceUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockListingRepository stockListingRepository;
    private final StockMapper stockMapper;
    private final StockExchangeMapper stockExchangeMapper;
    private final StockExchangeService stockExchangeService;


    public Page<StockDto> getAllStocks(
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Stock> stockPage = stockRepository.findAll(pageable);
        return stockPage.map(stockMapper::map);
    }

    public Page<StockExchangeDto> getAllStockExchangesByStock(Long stockId, int page, int size) {
        if (!stockRepository.existsById(stockId)) {
            throw new ResourceNotFoundException("Stock not found with id: " + stockId);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<StockExchange> stockExchangePage = stockListingRepository.findStockExchangesByStockId(stockId, pageable);

        return stockExchangePage.map(stockExchangeMapper::map);
    }

    @Transactional
    public StockDto createStock(StockCreationRequest stockCreationRequest) {
        // Check if stock with same symbol already exists
        if (stockRepository.existsByName(stockCreationRequest.getName())) {
            throw new DuplicateResourceException(
                    "Stock with name " + stockCreationRequest.getName() + " already exists");
        }

        Stock stock = stockMapper.map(stockCreationRequest);
        Stock savedStock = stockRepository.save(stock);
        return stockMapper.map(savedStock);
    }

    @Transactional
    public StockDto updatePrice(Long stockId, StockPriceUpdateRequest stockPriceUpdateRequest) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found with id: " + stockId));

        // Update only the price field
        stock.setCurrentPrice(stockPriceUpdateRequest.getCurrentPrice());
        // No need to call save() - @Transactional handles it with dirty checking
        return stockMapper.map(stock);
    }

    @Transactional(readOnly = true)
    public StockDto getStockById(Long stockId) {
        return stockRepository.findById(stockId)
                .map(stockMapper::map)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found with id: " + stockId));
    }

    @Transactional
    public void deleteStock(Long stockId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found with id: " + stockId));

        List<StockExchange> affectedExchanges = stock.getStockListings().stream()
                .map(StockListing::getStockExchange)
                .distinct()
                .toList();

        stockRepository.delete(stock);

        affectedExchanges.forEach(stockExchangeService::updateLiveMarketStatus);
    }
}
