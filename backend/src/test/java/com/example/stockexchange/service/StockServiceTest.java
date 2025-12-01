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
@DisplayName("StockExchangeService Tests")
class StockExchangeServiceTest {

    @Mock
    private StockExchangeRepository stockExchangeRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockListingRepository stockListingRepository;

    @Mock
    private StockExchangeMapper stockExchangeMapper;

    @Mock
    private StockMapper stockMapper;

    @InjectMocks
    private StockExchangeService stockExchangeService;

    private StockExchange stockExchange;
    private StockExchangeDto stockExchangeDto;
    private Stock stock;
    private StockDto stockDto;
    private StockListing stockListing;

    @BeforeEach
    void setUp() {
        // Setup StockExchange entity
        stockExchange = new StockExchange();
        stockExchange.setStockExchangeId(1L);
        stockExchange.setName("NYSE");
        stockExchange.setDescription("New York Stock Exchange");
        stockExchange.setLiveInMarket(true);

        // Setup StockExchangeDto
        stockExchangeDto = new StockExchangeDto();
        stockExchangeDto.setStockExchangeId(1L);
        stockExchangeDto.setName("NYSE");
        stockExchangeDto.setDescription("New York Stock Exchange");
        stockExchangeDto.setLiveInMarket(true);

        // Setup Stock entity
        stock = new Stock();
        stock.setStockId(1L);
        stock.setName("Apple Inc.");
        stock.setDescription("Technology company");
        stock.setCurrentPrice(BigDecimal.valueOf(150.00));

        // Setup StockDto
        stockDto = new StockDto();
        stockDto.setStockId(1L);
        stockDto.setName("Apple Inc.");
        stockDto.setDescription("Technology company");
        stockDto.setCurrentPrice(BigDecimal.valueOf(150.00));

        // Setup StockListing
        stockListing = new StockListing(stockExchange, stock);
    }

    @Nested
    @DisplayName("Get All Stock Exchanges Tests")
    class GetAllStockExchangesTests {

        @Test
        @DisplayName("Should return all stock exchanges with pagination")
        void getAllStockExchanges_Success() {
            // Arrange
            List<StockExchange> exchanges = List.of(stockExchange);
            Page<StockExchange> exchangePage = new PageImpl<>(exchanges, PageRequest.of(0, 10), 1);

            when(stockExchangeRepository.findAll(any(Pageable.class))).thenReturn(exchangePage);
            when(stockExchangeMapper.map(any(StockExchange.class))).thenReturn(stockExchangeDto);

            // Act
            Page<StockExchangeDto> result = stockExchangeService.getAllStockExchanges(0, 10);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("NYSE", result.getContent().get(0).getName());

            verify(stockExchangeRepository).findAll(any(Pageable.class));
            verify(stockExchangeMapper).map(any(StockExchange.class));
        }

        @Test
        @DisplayName("Should return empty page when no exchanges exist")
        void getAllStockExchanges_EmptyPage() {
            // Arrange
            Page<StockExchange> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            when(stockExchangeRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            // Act
            Page<StockExchangeDto> result = stockExchangeService.getAllStockExchanges(0, 10);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());

            verify(stockExchangeRepository).findAll(any(Pageable.class));
            verify(stockExchangeMapper, never()).map(any(StockExchange.class));
        }

        @Test
        @DisplayName("Should handle pagination correctly")
        void getAllStockExchanges_Pagination() {
            // Arrange
            List<StockExchange> exchanges = List.of(stockExchange);
            Page<StockExchange> exchangePage = new PageImpl<>(exchanges, PageRequest.of(2, 5), 20);

            when(stockExchangeRepository.findAll(any(Pageable.class))).thenReturn(exchangePage);
            when(stockExchangeMapper.map(any(StockExchange.class))).thenReturn(stockExchangeDto);

            // Act
            Page<StockExchangeDto> result = stockExchangeService.getAllStockExchanges(2, 5);

            // Assert
            assertNotNull(result);
            assertEquals(20, result.getTotalElements());
            assertEquals(4, result.getTotalPages());
            assertEquals(2, result.getNumber());
            assertEquals(5, result.getSize());

            verify(stockExchangeRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return multiple stock exchanges")
        void getAllStockExchanges_MultipleExchanges() {
            // Arrange
            StockExchange nasdaq = new StockExchange();
            nasdaq.setStockExchangeId(2L);
            nasdaq.setName("NASDAQ");

            StockExchangeDto nasdaqDto = new StockExchangeDto();
            nasdaqDto.setStockExchangeId(2L);
            nasdaqDto.setName("NASDAQ");

            List<StockExchange> exchanges = List.of(stockExchange, nasdaq);
            Page<StockExchange> exchangePage = new PageImpl<>(exchanges, PageRequest.of(0, 10), 2);

            when(stockExchangeRepository.findAll(any(Pageable.class))).thenReturn(exchangePage);
            when(stockExchangeMapper.map(any(StockExchange.class)))
                    .thenReturn(stockExchangeDto)
                    .thenReturn(nasdaqDto);

            // Act
            Page<StockExchangeDto> result = stockExchangeService.getAllStockExchanges(0, 10);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals(2, result.getContent().size());

            verify(stockExchangeMapper, times(2)).map(any(StockExchange.class));
        }
    }

    @Nested
    @DisplayName("Get All Stock Exchanges Live In Market Tests")
    class GetAllStockExchangesLiveInMarketTests {

        @Test
        @DisplayName("Should return only live stock exchanges")
        void getAllStockExchangesLiveInMarket_Success() {
            // Arrange
            stockExchange.setLiveInMarket(true);
            List<StockExchange> liveExchanges = List.of(stockExchange);
            Page<StockExchange> exchangePage = new PageImpl<>(liveExchanges, PageRequest.of(0, 10), 1);

            when(stockExchangeRepository.findByLiveInMarketTrue(any(Pageable.class))).thenReturn(exchangePage);
            when(stockExchangeMapper.map(any(StockExchange.class))).thenReturn(stockExchangeDto);

            // Act
            Page<StockExchangeDto> result = stockExchangeService.getAllStockExchangesLiveInMarket(0, 10);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertTrue(result.getContent().get(0).isLiveInMarket());

            verify(stockExchangeRepository).findByLiveInMarketTrue(any(Pageable.class));
            verify(stockExchangeMapper).map(any(StockExchange.class));
        }

        @Test
        @DisplayName("Should return empty page when no live exchanges exist")
        void getAllStockExchangesLiveInMarket_EmptyPage() {
            // Arrange
            Page<StockExchange> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            when(stockExchangeRepository.findByLiveInMarketTrue(any(Pageable.class))).thenReturn(emptyPage);

            // Act
            Page<StockExchangeDto> result = stockExchangeService.getAllStockExchangesLiveInMarket(0, 10);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());

            verify(stockExchangeRepository).findByLiveInMarketTrue(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle pagination for live exchanges")
        void getAllStockExchangesLiveInMarket_Pagination() {
            // Arrange
            List<StockExchange> liveExchanges = List.of(stockExchange);
            Page<StockExchange> exchangePage = new PageImpl<>(liveExchanges, PageRequest.of(1, 5), 15);

            when(stockExchangeRepository.findByLiveInMarketTrue(any(Pageable.class))).thenReturn(exchangePage);
            when(stockExchangeMapper.map(any(StockExchange.class))).thenReturn(stockExchangeDto);

            // Act
            Page<StockExchangeDto> result = stockExchangeService.getAllStockExchangesLiveInMarket(1, 5);

            // Assert
            assertNotNull(result);
            assertEquals(15, result.getTotalElements());
            assertEquals(3, result.getTotalPages());

            verify(stockExchangeRepository).findByLiveInMarketTrue(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return multiple live exchanges")
        void getAllStockExchangesLiveInMarket_MultipleLiveExchanges() {
            // Arrange
            StockExchange nasdaq = new StockExchange();
            nasdaq.setStockExchangeId(2L);
            nasdaq.setName("NASDAQ");
            nasdaq.setLiveInMarket(true);

            StockExchangeDto nasdaqDto = new StockExchangeDto();
            nasdaqDto.setStockExchangeId(2L);
            nasdaqDto.setName("NASDAQ");
            nasdaqDto.setLiveInMarket(true);

            List<StockExchange> liveExchanges = List.of(stockExchange, nasdaq);
            Page<StockExchange> exchangePage = new PageImpl<>(liveExchanges, PageRequest.of(0, 10), 2);

            when(stockExchangeRepository.findByLiveInMarketTrue(any(Pageable.class))).thenReturn(exchangePage);
            when(stockExchangeMapper.map(any(StockExchange.class)))
                    .thenReturn(stockExchangeDto)
                    .thenReturn(nasdaqDto);

            // Act
            Page<StockExchangeDto> result = stockExchangeService.getAllStockExchangesLiveInMarket(0, 10);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertTrue(result.getContent().stream().allMatch(StockExchangeDto::isLiveInMarket));

            verify(stockExchangeMapper, times(2)).map(any(StockExchange.class));
        }
    }

    @Nested
    @DisplayName("Create Stock Exchange Tests")
    class CreateStockExchangeTests {

        private StockExchangeCreationRequest creationRequest;

        @BeforeEach
        void setUp() {
            creationRequest = new StockExchangeCreationRequest();
            creationRequest.setName("NASDAQ");
            creationRequest.setDescription("Technology stock exchange");
        }

        @Test
        @DisplayName("Should create stock exchange successfully")
        void createStockExchange_Success() {
            // Arrange
            when(stockExchangeMapper.map(any(StockExchangeCreationRequest.class))).thenReturn(stockExchange);
            when(stockExchangeRepository.save(any(StockExchange.class))).thenReturn(stockExchange);
            when(stockExchangeMapper.map(any(StockExchange.class))).thenReturn(stockExchangeDto);

            // Act
            StockExchangeDto result = stockExchangeService.createStockExchange(creationRequest);

            // Assert
            assertNotNull(result);
            assertEquals("NYSE", result.getName());
            assertEquals("New York Stock Exchange", result.getDescription());

            verify(stockExchangeMapper).map(any(StockExchangeCreationRequest.class));
            verify(stockExchangeRepository).save(any(StockExchange.class));
            verify(stockExchangeMapper).map(any(StockExchange.class));
        }

        @Test
        @DisplayName("Should create stock exchange with default live status false")
        void createStockExchange_DefaultLiveStatusFalse() {
            // Arrange
            stockExchange.setLiveInMarket(false);
            stockExchangeDto.setLiveInMarket(false);

            when(stockExchangeMapper.map(any(StockExchangeCreationRequest.class))).thenReturn(stockExchange);
            when(stockExchangeRepository.save(any(StockExchange.class))).thenReturn(stockExchange);
            when(stockExchangeMapper.map(any(StockExchange.class))).thenReturn(stockExchangeDto);

            // Act
            StockExchangeDto result = stockExchangeService.createStockExchange(creationRequest);

            // Assert
            assertNotNull(result);
            assertFalse(result.isLiveInMarket());

            verify(stockExchangeRepository).save(any(StockExchange.class));
        }

        @Test
        @DisplayName("Should map request fields correctly")
        void createStockExchange_MapsFieldsCorrectly() {
            // Arrange
            StockExchange newExchange = new StockExchange();
            newExchange.setName("NASDAQ");
            newExchange.setDescription("Technology stock exchange");

            StockExchangeDto newDto = new StockExchangeDto();
            newDto.setName("NASDAQ");
            newDto.setDescription("Technology stock exchange");

            when(stockExchangeMapper.map(any(StockExchangeCreationRequest.class))).thenReturn(newExchange);
            when(stockExchangeRepository.save(any(StockExchange.class))).thenReturn(newExchange);
            when(stockExchangeMapper.map(any(StockExchange.class))).thenReturn(newDto);

            // Act
            StockExchangeDto result = stockExchangeService.createStockExchange(creationRequest);

            // Assert
            assertNotNull(result);
            assertEquals("NASDAQ", result.getName());
            assertEquals("Technology stock exchange", result.getDescription());
        }
    }

    @Nested
    @DisplayName("Update Stock Exchange Tests")
    class UpdateStockExchangeTests {

        private StockExchangeUpdateRequest updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = new StockExchangeUpdateRequest();
            updateRequest.setName("NYSE Updated");
            updateRequest.setDescription("Updated description");
        }



        @Test
        @DisplayName("Should throw exception when updating non-existent stock exchange")
        void updateStockExchange_NotFound() {
            // Arrange
            when(stockExchangeRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> stockExchangeService.updateStockExchange(999L, updateRequest)
            );

            assertTrue(exception.getMessage().contains("999"));

            verify(stockExchangeRepository).findById(999L);
            verify(stockExchangeRepository, never()).save(any(StockExchange.class));
        }


    }

    @Nested
    @DisplayName("Delete Stock Exchange Tests")
    class DeleteStockExchangeTests {

        @Test
        @DisplayName("Should delete stock exchange successfully")
        void deleteStockExchange_Success() {
            // Arrange
            when(stockExchangeRepository.findById(1L)).thenReturn(Optional.of(stockExchange));
            doNothing().when(stockExchangeRepository).delete(any(StockExchange.class));

            // Act
            stockExchangeService.deleteStockExchange(1L);

            // Assert
            verify(stockExchangeRepository).findById(1L);
            verify(stockExchangeRepository).delete(stockExchange);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent stock exchange")
        void deleteStockExchange_NotFound() {
            // Arrange
            when(stockExchangeRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> stockExchangeService.deleteStockExchange(999L)
            );

            assertEquals("Stock Exchange not found with id: 999", exception.getMessage());
            verify(stockExchangeRepository).findById(999L);
            verify(stockExchangeRepository, never()).delete(any(StockExchange.class));
        }

        @Test
        @DisplayName("Should delete stock exchange and cascade delete listings")
        void deleteStockExchange_CascadeDelete() {
            // Arrange
            stockExchange.setStockListings(new ArrayList<>(List.of(stockListing)));
            when(stockExchangeRepository.findById(1L)).thenReturn(Optional.of(stockExchange));
            doNothing().when(stockExchangeRepository).delete(any(StockExchange.class));

            // Act
            stockExchangeService.deleteStockExchange(1L);

            // Assert
            verify(stockExchangeRepository).findById(1L);
            verify(stockExchangeRepository).delete(stockExchange);
        }

        @Test
        @DisplayName("Should delete stock exchange with null listings")
        void deleteStockExchange_NullListings() {
            // Arrange
            stockExchange.setStockListings(null);
            when(stockExchangeRepository.findById(1L)).thenReturn(Optional.of(stockExchange));
            doNothing().when(stockExchangeRepository).delete(any(StockExchange.class));

            // Act
            assertDoesNotThrow(() -> stockExchangeService.deleteStockExchange(1L));

            // Assert
            verify(stockExchangeRepository).findById(1L);
            verify(stockExchangeRepository).delete(stockExchange);
        }
    }

    @Nested
    @DisplayName("Get All Stocks By Exchange Tests")
    class GetAllStocksByExchangeTests {

        @Test
        @DisplayName("Should return stocks for valid stock exchange")
        void getAllStocksByExchange_Success() {
            // Arrange
            List<Stock> stocks = List.of(stock);
            Page<Stock> stockPage = new PageImpl<>(stocks, PageRequest.of(0, 10), 1);

            when(stockExchangeRepository.existsById(1L)).thenReturn(true);
            when(stockListingRepository.findStocksByStockExchangeId(anyLong(), any(Pageable.class)))
                    .thenReturn(stockPage);
            when(stockMapper.map(any(Stock.class))).thenReturn(stockDto);

            // Act
            Page<StockDto> result = stockExchangeService.getAllStocksByExchange(1L, 0, 10, "name");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("Apple Inc.", result.getContent().get(0).getName());

            verify(stockExchangeRepository).existsById(1L);
            verify(stockListingRepository).findStocksByStockExchangeId(anyLong(), any(Pageable.class));
            verify(stockMapper).map(any(Stock.class));
        }

        @Test
        @DisplayName("Should throw exception when stock exchange not found")
        void getAllStocksByExchange_ExchangeNotFound() {
            // Arrange
            when(stockExchangeRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> stockExchangeService.getAllStocksByExchange(999L, 0, 10, "name")
            );

            assertEquals("Stock Exchange not found with id: 999", exception.getMessage());

            verify(stockExchangeRepository).existsById(999L);
            verify(stockListingRepository, never()).findStocksByStockExchangeId(anyLong(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when exchange has no stocks")
        void getAllStocksByExchange_NoStocks() {
            // Arrange
            Page<Stock> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            when(stockExchangeRepository.existsById(1L)).thenReturn(true);
            when(stockListingRepository.findStocksByStockExchangeId(anyLong(), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // Act
            Page<StockDto> result = stockExchangeService.getAllStocksByExchange(1L, 0, 10, "name");

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());

            verify(stockExchangeRepository).existsById(1L);
            verify(stockListingRepository).findStocksByStockExchangeId(anyLong(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return stocks sorted by specified field")
        void getAllStocksByExchange_WithSorting() {
            // Arrange
            Stock stock2 = new Stock();
            stock2.setStockId(2L);
            stock2.setName("Microsoft Corp.");

            StockDto stockDto2 = new StockDto();
            stockDto2.setStockId(2L);
            stockDto2.setName("Microsoft Corp.");

            List<Stock> stocks = List.of(stock, stock2);
            Page<Stock> stockPage = new PageImpl<>(stocks, PageRequest.of(0, 10), 2);

            when(stockExchangeRepository.existsById(1L)).thenReturn(true);
            when(stockListingRepository.findStocksByStockExchangeId(anyLong(), any(Pageable.class)))
                    .thenReturn(stockPage);
            when(stockMapper.map(any(Stock.class)))
                    .thenReturn(stockDto)
                    .thenReturn(stockDto2);

            // Act
            Page<StockDto> result = stockExchangeService.getAllStocksByExchange(1L, 0, 10, "name");

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals(2, result.getContent().size());

            verify(stockExchangeRepository).existsById(1L);
            verify(stockListingRepository).findStocksByStockExchangeId(anyLong(), any(Pageable.class));
            verify(stockMapper, times(2)).map(any(Stock.class));
        }

        @Test
        @DisplayName("Should handle pagination correctly")
        void getAllStocksByExchange_Pagination() {
            // Arrange
            List<Stock> stocks = List.of(stock);
            Page<Stock> stockPage = new PageImpl<>(stocks, PageRequest.of(1, 5), 20);

            when(stockExchangeRepository.existsById(1L)).thenReturn(true);
            when(stockListingRepository.findStocksByStockExchangeId(anyLong(), any(Pageable.class)))
                    .thenReturn(stockPage);
            when(stockMapper.map(any(Stock.class))).thenReturn(stockDto);

            // Act
            Page<StockDto> result = stockExchangeService.getAllStocksByExchange(1L, 1, 5, "name");

            // Assert
            assertNotNull(result);
            assertEquals(20, result.getTotalElements());
            assertEquals(4, result.getTotalPages());
            assertEquals(1, result.getNumber());
            assertEquals(5, result.getSize());

            verify(stockExchangeRepository).existsById(1L);
        }

        @Test
        @DisplayName("Should sort by price when specified")
        void getAllStocksByExchange_SortByPrice() {
            // Arrange
            List<Stock> stocks = List.of(stock);
            Page<Stock> stockPage = new PageImpl<>(stocks, PageRequest.of(0, 10), 1);

            when(stockExchangeRepository.existsById(1L)).thenReturn(true);
            when(stockListingRepository.findStocksByStockExchangeId(anyLong(), any(Pageable.class)))
                    .thenReturn(stockPage);
            when(stockMapper.map(any(Stock.class))).thenReturn(stockDto);

            // Act
            Page<StockDto> result = stockExchangeService.getAllStocksByExchange(1L, 0, 10, "currentPrice");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());

            verify(stockListingRepository).findStocksByStockExchangeId(anyLong(), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Add Stock To Stock Exchange Tests")
    class AddStockToStockExchangeTests {

        @Nested
        @DisplayName("Live Market Status Update Tests")
        class LiveMarketStatusTests {

            @Test
            @DisplayName("Should update live market status when adding 10th stock")
            void addStockToStockExchange_UpdateLiveStatusToTrue() {
                // Arrange
                stockExchange.setLiveInMarket(false);

                when(stockListingRepository.existsById(any(StockListingId.class))).thenReturn(false);
                when(stockExchangeRepository.findById(1L)).thenReturn(Optional.of(stockExchange));
                when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
                when(stockListingRepository.save(any(StockListing.class))).thenReturn(stockListing);
                when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(10L);
                when(stockExchangeMapper.map(any(StockExchange.class))).thenReturn(stockExchangeDto);
                when(stockMapper.map(any(Stock.class))).thenReturn(stockDto);

                // Act
                StockListingDto result = stockExchangeService.addStockToStockExchange(1L, 1L);

                // Assert
                assertNotNull(result);
                assertTrue(stockExchange.isLiveInMarket());
                verify(stockListingRepository).countByStockExchangeId(1L);
            }

            @Test
            @DisplayName("Should not change live status when adding stock but count still below 10")
            void addStockToStockExchange_NoLiveStatusChange() {
                // Arrange
                stockExchange.setLiveInMarket(false);

                when(stockListingRepository.existsById(any(StockListingId.class))).thenReturn(false);
                when(stockExchangeRepository.findById(1L)).thenReturn(Optional.of(stockExchange));
                when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
                when(stockListingRepository.save(any(StockListing.class))).thenReturn(stockListing);
                when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(5L);
                when(stockExchangeMapper.map(any(StockExchange.class))).thenReturn(stockExchangeDto);
                when(stockMapper.map(any(Stock.class))).thenReturn(stockDto);

                // Act
                StockListingDto result = stockExchangeService.addStockToStockExchange(1L, 1L);

                // Assert
                assertNotNull(result);
                assertFalse(stockExchange.isLiveInMarket());
                verify(stockListingRepository).countByStockExchangeId(1L);
            }

            @Test
            @DisplayName("Should maintain live status when already live and adding more stocks")
            void addStockToStockExchange_MaintainLiveStatus() {
                // Arrange
                stockExchange.setLiveInMarket(true);

                when(stockListingRepository.existsById(any(StockListingId.class))).thenReturn(false);
                when(stockExchangeRepository.findById(1L)).thenReturn(Optional.of(stockExchange));
                when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
                when(stockListingRepository.save(any(StockListing.class))).thenReturn(stockListing);
                when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(15L);
                when(stockExchangeMapper.map(any(StockExchange.class))).thenReturn(stockExchangeDto);
                when(stockMapper.map(any(Stock.class))).thenReturn(stockDto);

                // Act
                StockListingDto result = stockExchangeService.addStockToStockExchange(1L, 1L);

                // Assert
                assertNotNull(result);
                assertTrue(stockExchange.isLiveInMarket());
                verify(stockListingRepository).countByStockExchangeId(1L);
            }
        }
    }

    @Nested
    @DisplayName("Remove Stock From Stock Exchange Tests")
    class RemoveStockFromStockExchangeTests {

        @Test
        @DisplayName("Should remove stock from stock exchange successfully")
        void removeStockFromStockExchange_Success() {
            // Arrange
            when(stockExchangeRepository.findById(1L)).thenReturn(Optional.of(stockExchange));
            when(stockListingRepository.findById(any(StockListingId.class)))
                    .thenReturn(Optional.of(stockListing));
            doNothing().when(stockListingRepository).delete(any(StockListing.class));
            when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(15L);

            // Act
            stockExchangeService.removeStockFromStockExchange(1L, 1L);

            // Assert
            verify(stockExchangeRepository).findById(1L);
            verify(stockListingRepository).findById(any(StockListingId.class));
            verify(stockListingRepository).delete(stockListing);
            verify(stockListingRepository).countByStockExchangeId(1L);
        }

        @Test
        @DisplayName("Should throw exception when stock exchange not found")
        void removeStockFromStockExchange_ExchangeNotFound() {
            // Arrange
            when(stockExchangeRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> stockExchangeService.removeStockFromStockExchange(999L, 1L)
            );

            assertEquals("Stock Exchange not found with id: 999", exception.getMessage());

            verify(stockExchangeRepository).findById(999L);
            verify(stockListingRepository, never()).findById(any(StockListingId.class));
            verify(stockListingRepository, never()).delete(any(StockListing.class));
        }

        @Test
        @DisplayName("Should throw exception when stock not listed on exchange")
        void removeStockFromStockExchange_StockNotListed() {
            // Arrange
            when(stockExchangeRepository.findById(1L)).thenReturn(Optional.of(stockExchange));
            when(stockListingRepository.findById(any(StockListingId.class)))
                    .thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> stockExchangeService.removeStockFromStockExchange(1L, 1L)
            );

            assertEquals("Stock with id 1 is not listed on this Stock Exchange",
                    exception.getMessage());

            verify(stockExchangeRepository).findById(1L);
            verify(stockListingRepository).findById(any(StockListingId.class));
            verify(stockListingRepository, never()).delete(any(StockListing.class));
        }

        @Nested
        @DisplayName("Live Market Status Update Tests")
        class LiveMarketStatusTests {

            @Test
            @DisplayName("Should update live status to false when stocks fall below 10")
            void removeStockFromStockExchange_UpdateLiveStatusToFalse() {
                // Arrange
                stockExchange.setLiveInMarket(true);

                when(stockExchangeRepository.findById(1L)).thenReturn(Optional.of(stockExchange));
                when(stockListingRepository.findById(any(StockListingId.class)))
                        .thenReturn(Optional.of(stockListing));
                doNothing().when(stockListingRepository).delete(any(StockListing.class));
                when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(9L);

                // Act
                stockExchangeService.removeStockFromStockExchange(1L, 1L);

                // Assert
                assertFalse(stockExchange.isLiveInMarket());
                verify(stockListingRepository).countByStockExchangeId(1L);
                verify(stockListingRepository).delete(stockListing);
            }

            @Test
            @DisplayName("Should not change live status when removing stock but count still >= 10")
            void removeStockFromStockExchange_NoLiveStatusChange() {
                // Arrange
                stockExchange.setLiveInMarket(true);

                when(stockExchangeRepository.findById(1L)).thenReturn(Optional.of(stockExchange));
                when(stockListingRepository.findById(any(StockListingId.class)))
                        .thenReturn(Optional.of(stockListing));
                doNothing().when(stockListingRepository).delete(any(StockListing.class));
                when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(10L);

                // Act
                stockExchangeService.removeStockFromStockExchange(1L, 1L);

                // Assert
                assertTrue(stockExchange.isLiveInMarket());
                verify(stockListingRepository).countByStockExchangeId(1L);
            }

            @Test
            @DisplayName("Should handle removing last stock from exchange")
            void removeStockFromStockExchange_LastStock() {
                // Arrange
                stockExchange.setLiveInMarket(false);

                when(stockExchangeRepository.findById(1L)).thenReturn(Optional.of(stockExchange));
                when(stockListingRepository.findById(any(StockListingId.class)))
                        .thenReturn(Optional.of(stockListing));
                doNothing().when(stockListingRepository).delete(any(StockListing.class));
                when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(0L);

                // Act
                stockExchangeService.removeStockFromStockExchange(1L, 1L);

                // Assert
                assertFalse(stockExchange.isLiveInMarket());
                verify(stockListingRepository).delete(stockListing);
            }

            @Test
            @DisplayName("Should maintain not live status when removing from already not live exchange")
            void removeStockFromStockExchange_MaintainNotLiveStatus() {
                // Arrange
                stockExchange.setLiveInMarket(false);

                when(stockExchangeRepository.findById(1L)).thenReturn(Optional.of(stockExchange));
                when(stockListingRepository.findById(any(StockListingId.class)))
                        .thenReturn(Optional.of(stockListing));
                doNothing().when(stockListingRepository).delete(any(StockListing.class));
                when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(5L);

                // Act
                stockExchangeService.removeStockFromStockExchange(1L, 1L);

                // Assert
                assertFalse(stockExchange.isLiveInMarket());
                verify(stockListingRepository).countByStockExchangeId(1L);
            }
        }
    }

    @Nested
    @DisplayName("Update Live Market Status Tests")
    class UpdateLiveMarketStatusTests {

        @Test
        @DisplayName("Should set live status to true when stocks >= 10")
        void updateLiveMarketStatus_SetToTrue() {
            // Arrange
            stockExchange.setLiveInMarket(false);
            when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(10L);

            // Act
            stockExchangeService.updateLiveMarketStatus(stockExchange);

            // Assert
            assertTrue(stockExchange.isLiveInMarket());
            verify(stockListingRepository).countByStockExchangeId(1L);
        }

        @Test
        @DisplayName("Should set live status to false when stocks < 10")
        void updateLiveMarketStatus_SetToFalse() {
            // Arrange
            stockExchange.setLiveInMarket(true);
            when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(9L);

            // Act
            stockExchangeService.updateLiveMarketStatus(stockExchange);

            // Assert
            assertFalse(stockExchange.isLiveInMarket());
            verify(stockListingRepository).countByStockExchangeId(1L);
        }

        @Test
        @DisplayName("Should not change status when already correct (live with 10+ stocks)")
        void updateLiveMarketStatus_NoChangeWhenAlreadyLive() {
            // Arrange
            stockExchange.setLiveInMarket(true);
            when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(15L);

            // Act
            stockExchangeService.updateLiveMarketStatus(stockExchange);

            // Assert
            assertTrue(stockExchange.isLiveInMarket());
            verify(stockListingRepository).countByStockExchangeId(1L);
        }

        @Test
        @DisplayName("Should not change status when already correct (not live with < 10 stocks)")
        void updateLiveMarketStatus_NoChangeWhenAlreadyNotLive() {
            // Arrange
            stockExchange.setLiveInMarket(false);
            when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(5L);

            // Act
            stockExchangeService.updateLiveMarketStatus(stockExchange);

            // Assert
            assertFalse(stockExchange.isLiveInMarket());
            verify(stockListingRepository).countByStockExchangeId(1L);
        }

        @Nested
        @DisplayName("Edge Cases")
        class EdgeCaseTests {

            @Test
            @DisplayName("Should handle exactly 10 stocks")
            void updateLiveMarketStatus_ExactlyTenStocks() {
                // Arrange
                stockExchange.setLiveInMarket(false);
                when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(10L);

                // Act
                stockExchangeService.updateLiveMarketStatus(stockExchange);

                // Assert
                assertTrue(stockExchange.isLiveInMarket());
                verify(stockListingRepository).countByStockExchangeId(1L);
            }

            @Test
            @DisplayName("Should handle zero stocks")
            void updateLiveMarketStatus_ZeroStocks() {
                // Arrange
                stockExchange.setLiveInMarket(true);
                when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(0L);

                // Act
                stockExchangeService.updateLiveMarketStatus(stockExchange);

                // Assert
                assertFalse(stockExchange.isLiveInMarket());
                verify(stockListingRepository).countByStockExchangeId(1L);
            }

            @Test
            @DisplayName("Should handle large number of stocks")
            void updateLiveMarketStatus_LargeNumberOfStocks() {
                // Arrange
                stockExchange.setLiveInMarket(false);
                when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(1000L);

                // Act
                stockExchangeService.updateLiveMarketStatus(stockExchange);

                // Assert
                assertTrue(stockExchange.isLiveInMarket());
                verify(stockListingRepository).countByStockExchangeId(1L);
            }

            @Test
            @DisplayName("Should handle transition from live to not live")
            void updateLiveMarketStatus_TransitionFromLiveToNotLive() {
                // Arrange
                stockExchange.setLiveInMarket(true);
                when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(3L);

                // Act
                stockExchangeService.updateLiveMarketStatus(stockExchange);

                // Assert
                assertFalse(stockExchange.isLiveInMarket());
                verify(stockListingRepository).countByStockExchangeId(1L);
            }

            @Test
            @DisplayName("Should handle transition from not live to live")
            void updateLiveMarketStatus_TransitionFromNotLiveToLive() {
                // Arrange
                stockExchange.setLiveInMarket(false);
                when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(20L);

                // Act
                stockExchangeService.updateLiveMarketStatus(stockExchange);

                // Assert
                assertTrue(stockExchange.isLiveInMarket());
                verify(stockListingRepository).countByStockExchangeId(1L);
            }

            @Test
            @DisplayName("Should handle boundary case with 9 stocks")
            void updateLiveMarketStatus_NineStocks() {
                // Arrange
                stockExchange.setLiveInMarket(true);
                when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(9L);

                // Act
                stockExchangeService.updateLiveMarketStatus(stockExchange);

                // Assert
                assertFalse(stockExchange.isLiveInMarket());
                verify(stockListingRepository).countByStockExchangeId(1L);
            }

            @Test
            @DisplayName("Should handle boundary case with 11 stocks")
            void updateLiveMarketStatus_ElevenStocks() {
                // Arrange
                stockExchange.setLiveInMarket(false);
                when(stockListingRepository.countByStockExchangeId(1L)).thenReturn(11L);

                // Act
                stockExchangeService.updateLiveMarketStatus(stockExchange);

                // Assert
                assertTrue(stockExchange.isLiveInMarket());
                verify(stockListingRepository).countByStockExchangeId(1L);
            }
        }
    }
}