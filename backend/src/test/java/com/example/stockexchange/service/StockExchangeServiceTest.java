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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockService Tests")
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockListingRepository stockListingRepository;

    @Mock
    private StockMapper stockMapper;

    @Mock
    private StockExchangeMapper stockExchangeMapper;

    @Mock
    private StockExchangeService stockExchangeService;

    @InjectMocks
    private StockService stockService;

    private Stock stock;
    private StockDto stockDto;
    private StockExchange stockExchange;
    private StockExchangeDto stockExchangeDto;
    private StockCreationRequest stockCreationRequest;
    private StockPriceUpdateRequest stockPriceUpdateRequest;

    @BeforeEach
    void setUp() {
        // Setup Stock entity
        stock = new Stock();
        stock.setStockId(1L);
        stock.setName("Apple Inc.");
        stock.setDescription("Technology company");
        stock.setCurrentPrice(BigDecimal.valueOf(150.00));
        stock.setStockListings(new ArrayList<>());

        // Setup StockDto
        stockDto = new StockDto();
        stockDto.setStockId(1L);
        stockDto.setName("Apple Inc.");
        stockDto.setDescription("Technology company");
        stockDto.setCurrentPrice(BigDecimal.valueOf(150.00));

        // Setup StockExchange
        stockExchange = new StockExchange();
        stockExchange.setStockExchangeId(1L);
        stockExchange.setName("NYSE");
        stockExchange.setLiveInMarket(true);

        // Setup StockExchangeDto
        stockExchangeDto = new StockExchangeDto();
        stockExchangeDto.setStockExchangeId(1L);
        stockExchangeDto.setName("NYSE");
        stockExchangeDto.setLiveInMarket(true);

        // Setup requests
        stockCreationRequest = new StockCreationRequest();
        stockCreationRequest.setName("Apple Inc.");
        stockCreationRequest.setDescription("Technology company");
        stockCreationRequest.setCurrentPrice(BigDecimal.valueOf(150.00));

        stockPriceUpdateRequest = new StockPriceUpdateRequest();
        stockPriceUpdateRequest.setCurrentPrice(BigDecimal.valueOf(160.00));
    }

    @Nested
    @DisplayName("getAllStocks Tests")
    class GetAllStocksTests {

        @Test
        @DisplayName("Should return paginated stocks with ascending sort")
        void shouldReturnPaginatedStocksSuccessfully() {
            // Arrange
            List<Stock> stocks = List.of(stock);
            Page<Stock> stockPage = new PageImpl<>(stocks, PageRequest.of(0, 10), 1);

            when(stockRepository.findAll(any(Pageable.class))).thenReturn(stockPage);
            when(stockMapper.map(any(Stock.class))).thenReturn(stockDto);

            // Act
            Page<StockDto> result = stockService.getAllStocks(0, 10, "name", "asc");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(1, result.getContent().size());
            assertEquals("Apple Inc.", result.getContent().get(0).getName());

            verify(stockRepository, times(1)).findAll(any(Pageable.class));
            verify(stockMapper, times(1)).map(any(Stock.class));
        }

        @Test
        @DisplayName("Should return paginated stocks when no specific sort field is given")
        void shouldReturnPaginatedStocksWithDefaultSort() {
            // Arrange
            List<Stock> stocks = List.of(stock);
            Page<Stock> stockPage = new PageImpl<>(stocks, PageRequest.of(0, 10), 1);

            when(stockRepository.findAll(any(Pageable.class))).thenReturn(stockPage);
            when(stockMapper.map(any(Stock.class))).thenReturn(stockDto);

            // Act
            Page<StockDto> result = stockService.getAllStocks(0, 10, "stockName", "asc");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(1, result.getContent().size());
            assertEquals("Apple Inc.", result.getContent().get(0).getName());

            verify(stockRepository, times(1)).findAll(any(Pageable.class));
            verify(stockMapper, times(1)).map(any(Stock.class));
        }

        @Test
        @DisplayName("Should return empty page when no stocks exist")
        void shouldReturnEmptyPageWhenNoStocks() {
            // Arrange
            Page<Stock> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(stockRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            // Act
            Page<StockDto> result = stockService.getAllStocks(0, 10, "name", "asc");

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());

            verify(stockRepository, times(1)).findAll(any(Pageable.class));
            verify(stockMapper, never()).map(any(Stock.class));
        }

        @Test
        @DisplayName("Should sort stocks in descending order")
        void shouldSortStocksInDescendingOrder() {
            // Arrange
            List<Stock> stocks = List.of(stock);
            Page<Stock> stockPage = new PageImpl<>(stocks, PageRequest.of(0, 10), 1);

            when(stockRepository.findAll(any(Pageable.class))).thenReturn(stockPage);
            when(stockMapper.map(any(Stock.class))).thenReturn(stockDto);

            // Act
            Page<StockDto> result = stockService.getAllStocks(0, 10, "currentPrice", "desc");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());

            verify(stockRepository, times(1)).findAll(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("getAllStockExchangesByStock Tests")
    class GetAllStockExchangesByStockTests {

        @Test
        @DisplayName("Should return stock exchanges for valid stock")
        void shouldReturnStockExchangesForValidStock() {
            // Arrange
            List<StockExchange> exchanges = List.of(stockExchange);
            Page<StockExchange> exchangePage = new PageImpl<>(exchanges, PageRequest.of(0, 10), 1);

            when(stockRepository.existsById(1L)).thenReturn(true);
            when(stockListingRepository.findStockExchangesByStockId(anyLong(), any(Pageable.class)))
                    .thenReturn(exchangePage);
            when(stockExchangeMapper.map(any(StockExchange.class))).thenReturn(stockExchangeDto);

            // Act
            Page<StockExchangeDto> result = stockService.getAllStockExchangesByStock(1L, 0, 10);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("NYSE", result.getContent().get(0).getName());

            verify(stockRepository, times(1)).existsById(1L);
            verify(stockListingRepository, times(1))
                    .findStockExchangesByStockId(anyLong(), any(Pageable.class));
            verify(stockExchangeMapper, times(1)).map(any(StockExchange.class));
        }

        @Test
        @DisplayName("Should throw exception when stock not found")
        void shouldThrowExceptionWhenStockNotFound() {
            // Arrange
            when(stockRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> stockService.getAllStockExchangesByStock(999L, 0, 10)
            );

            assertEquals("Stock not found with id: 999", exception.getMessage());

            verify(stockRepository, times(1)).existsById(999L);
            verify(stockListingRepository, never())
                    .findStockExchangesByStockId(anyLong(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when stock has no listings")
        void shouldReturnEmptyPageWhenNoListings() {
            // Arrange
            Page<StockExchange> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            when(stockRepository.existsById(1L)).thenReturn(true);
            when(stockListingRepository.findStockExchangesByStockId(anyLong(), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // Act
            Page<StockExchangeDto> result = stockService.getAllStockExchangesByStock(1L, 0, 10);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());

            verify(stockRepository, times(1)).existsById(1L);
            verify(stockListingRepository, times(1))
                    .findStockExchangesByStockId(anyLong(), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("createStock Tests")
    class CreateStockTests {

        @Test
        @DisplayName("Should create stock successfully")
        void shouldCreateStockSuccessfully() {
            // Arrange
            when(stockRepository.existsByName("Apple Inc.")).thenReturn(false);
            when(stockMapper.map(any(StockCreationRequest.class))).thenReturn(stock);
            when(stockRepository.save(any(Stock.class))).thenReturn(stock);
            when(stockMapper.map(any(Stock.class))).thenReturn(stockDto);

            // Act
            StockDto result = stockService.createStock(stockCreationRequest);

            // Assert
            assertNotNull(result);
            assertEquals("Apple Inc.", result.getName());
            assertEquals(BigDecimal.valueOf(150.00), result.getCurrentPrice());

            verify(stockRepository, times(1)).existsByName("Apple Inc.");
            verify(stockRepository, times(1)).save(any(Stock.class));
            verify(stockMapper, times(1)).map(any(StockCreationRequest.class));
            verify(stockMapper, times(1)).map(any(Stock.class));
        }

        @Test
        @DisplayName("Should throw exception when stock name already exists")
        void shouldThrowExceptionWhenDuplicateName() {
            // Arrange
            when(stockRepository.existsByName("Apple Inc.")).thenReturn(true);

            // Act & Assert
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> stockService.createStock(stockCreationRequest)
            );

            assertEquals("Stock with name Apple Inc. already exists", exception.getMessage());

            verify(stockRepository, times(1)).existsByName("Apple Inc.");
            verify(stockRepository, never()).save(any(Stock.class));
        }

        @Test
        @DisplayName("Should create stock with valid price")
        void shouldCreateStockWithValidPrice() {
            // Arrange
            stockCreationRequest.setCurrentPrice(BigDecimal.valueOf(200.00));
            stock.setCurrentPrice(BigDecimal.valueOf(200.00));
            stockDto.setCurrentPrice(BigDecimal.valueOf(200.00));

            when(stockRepository.existsByName("Apple Inc.")).thenReturn(false);
            when(stockMapper.map(any(StockCreationRequest.class))).thenReturn(stock);
            when(stockRepository.save(any(Stock.class))).thenReturn(stock);
            when(stockMapper.map(any(Stock.class))).thenReturn(stockDto);

            // Act
            StockDto result = stockService.createStock(stockCreationRequest);

            // Assert
            assertNotNull(result);
            assertEquals(BigDecimal.valueOf(200.00), result.getCurrentPrice());

            verify(stockRepository, times(1)).save(any(Stock.class));
        }
    }

    @Nested
    @DisplayName("updatePrice Tests")
    class UpdatePriceTests {

        @Test
        @DisplayName("Should update stock price successfully")
        void shouldUpdatePriceSuccessfully() {
            // Arrange
            when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
            when(stockMapper.map(any(Stock.class))).thenReturn(stockDto);

            // Act
            StockDto result = stockService.updatePrice(1L, stockPriceUpdateRequest);

            // Assert
            assertNotNull(result);
            assertEquals(BigDecimal.valueOf(160.00), stock.getCurrentPrice());

            verify(stockRepository, times(1)).findById(1L);
            verify(stockMapper, times(1)).map(any(Stock.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent stock")
        void shouldThrowExceptionWhenStockNotFound() {
            // Arrange
            when(stockRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> stockService.updatePrice(999L, stockPriceUpdateRequest)
            );

            assertEquals("Stock not found with id: 999", exception.getMessage());

            verify(stockRepository, times(1)).findById(999L);
            verify(stockMapper, never()).map(any(Stock.class));
        }

        @Test
        @DisplayName("Should update price to zero")
        void shouldUpdatePriceToZero() {
            // Arrange
            stockPriceUpdateRequest.setCurrentPrice(BigDecimal.ZERO);

            when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
            when(stockMapper.map(any(Stock.class))).thenReturn(stockDto);

            // Act
            StockDto result = stockService.updatePrice(1L, stockPriceUpdateRequest);

            // Assert
            assertNotNull(result);
            assertEquals(BigDecimal.ZERO, stock.getCurrentPrice());

            verify(stockRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should update price with large value")
        void shouldUpdatePriceWithLargeValue() {
            // Arrange
            stockPriceUpdateRequest.setCurrentPrice(BigDecimal.valueOf(999999.99));

            when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
            when(stockMapper.map(any(Stock.class))).thenReturn(stockDto);

            // Act
            StockDto result = stockService.updatePrice(1L, stockPriceUpdateRequest);

            // Assert
            assertNotNull(result);
            assertEquals(BigDecimal.valueOf(999999.99), stock.getCurrentPrice());

            verify(stockRepository, times(1)).findById(1L);
        }
    }

    @Nested
    @DisplayName("deleteStock Tests")
    class DeleteStockTests {

        @Test
        @DisplayName("Should delete stock successfully")
        void shouldDeleteStockSuccessfully() {
            // Arrange
            when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
            doNothing().when(stockRepository).delete(any(Stock.class));

            // Act
            stockService.deleteStock(1L);

            // Assert
            verify(stockRepository, times(1)).findById(1L);
            verify(stockRepository, times(1)).delete(stock);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent stock")
        void shouldThrowExceptionWhenStockNotFound() {
            // Arrange
            when(stockRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> stockService.deleteStock(999L)
            );

            assertEquals("Stock not found with id: 999", exception.getMessage());

            verify(stockRepository, times(1)).findById(999L);
            verify(stockRepository, never()).delete(any(Stock.class));
        }

        @Test
        @DisplayName("Should delete stock and update affected exchanges")
        void shouldDeleteStockAndUpdateAffectedExchanges() {
            // Arrange
            StockListing listing1 = new StockListing();
            listing1.setStockExchange(stockExchange);
            listing1.setStock(stock);

            StockExchange stockExchange2 = new StockExchange();
            stockExchange2.setStockExchangeId(2L);
            stockExchange2.setName("NASDAQ");

            StockListing listing2 = new StockListing();
            listing2.setStockExchange(stockExchange2);
            listing2.setStock(stock);

            stock.setStockListings(List.of(listing1, listing2));

            when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
            doNothing().when(stockRepository).delete(any(Stock.class));
            doNothing().when(stockExchangeService).updateLiveMarketStatus(any(StockExchange.class));

            // Act
            stockService.deleteStock(1L);

            // Assert
            verify(stockRepository, times(1)).findById(1L);
            verify(stockRepository, times(1)).delete(stock);
            verify(stockExchangeService, times(2)).updateLiveMarketStatus(any(StockExchange.class));
        }

        @Test
        @DisplayName("Should delete stock with no listings")
        void shouldDeleteStockWithNoListings() {
            // Arrange
            stock.setStockListings(List.of());

            when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
            doNothing().when(stockRepository).delete(any(Stock.class));

            // Act
            stockService.deleteStock(1L);

            // Assert
            verify(stockRepository, times(1)).findById(1L);
            verify(stockRepository, times(1)).delete(stock);
            verify(stockExchangeService, never()).updateLiveMarketStatus(any(StockExchange.class));
        }

        @Test
        @DisplayName("Should delete stock and handle duplicate exchanges")
        void shouldDeleteStockAndHandleDuplicateExchanges() {
            // Arrange
            StockListing listing1 = new StockListing();
            listing1.setStockExchange(stockExchange);
            listing1.setStock(stock);

            StockListing listing2 = new StockListing();
            listing2.setStockExchange(stockExchange); // Same exchange
            listing2.setStock(stock);

            stock.setStockListings(List.of(listing1, listing2));

            when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
            doNothing().when(stockRepository).delete(any(Stock.class));
            doNothing().when(stockExchangeService).updateLiveMarketStatus(any(StockExchange.class));

            // Act
            stockService.deleteStock(1L);

            // Assert
            verify(stockRepository, times(1)).findById(1L);
            verify(stockRepository, times(1)).delete(stock);
            // Should only update once due to distinct()
            verify(stockExchangeService, times(1)).updateLiveMarketStatus(any(StockExchange.class));
        }
    }
}