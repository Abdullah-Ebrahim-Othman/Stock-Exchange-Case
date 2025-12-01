package com.example.stockexchange.controller;


import com.example.stockexchange.dto.StockDto;
import com.example.stockexchange.dto.StockExchangeDto;
import com.example.stockexchange.dto.StockListingDto;
import com.example.stockexchange.request.AddStocksToExchangeRequest;
import com.example.stockexchange.request.StockExchangeCreationRequest;
import com.example.stockexchange.request.StockExchangeUpdateRequest;
import com.example.stockexchange.response.ApiRespond;
import com.example.stockexchange.service.StockExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("${app.paths.api-base}${app.paths.api-version}/stockExchange")
@RequiredArgsConstructor
@Validated
@RestController
@Tag(name = "StockExchange Rest API Endpoints", description = "Operations related to StockExchanges in the system")
public class StockExchangeController {

    private final StockExchangeService stockExchangeService;

    @Operation(summary = "Get all Stock Exchanges", description = "Retrieves a paginated list of all Stock Exchanges")
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<ApiRespond> getAllStockExchanges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<StockExchangeDto> stockExchanges = stockExchangeService.getAllStockExchanges(page, size);

        return ResponseEntity.ok(new ApiRespond(
                HttpStatus.OK,
                "Stock Exchanges retrieved successfully",
                stockExchanges
        ));
    }
    
    @Operation(summary = "Get stocks not listed in a specific exchange", 
              description = "Retrieves a paginated list of stocks that are not listed in the specified stock exchange")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of stocks not in the exchange")
    @ApiResponse(responseCode = "404", description = "Stock exchange not found")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{exchangeId}/stocks/not-listed")
    public ResponseEntity<ApiRespond> getStocksNotInExchange(
            @PathVariable Long exchangeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
                
        Page<StockDto> stocks = stockExchangeService.findStocksNotInExchange(exchangeId, page, size);
        
        return ResponseEntity.ok(new ApiRespond(
                HttpStatus.OK,
                "Stocks not listed in exchange retrieved successfully",
                stocks
        ));
    }

    @Operation(summary = "Get Stock Exchange by ID", description = "Retrieves a specific Stock Exchange by its ID")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiRespond> getStockExchangeById(
            @PathVariable @Positive(message = "ID must be a positive number") Long id) {
        
        StockExchangeDto stockExchange = stockExchangeService.getStockExchangeById(id);
        
        return ResponseEntity.ok(new ApiRespond(
                HttpStatus.OK,
                "Stock Exchange retrieved successfully",
                stockExchange
        ));
    }

    @Operation(summary = "Get all live Stock Exchanges", description = "Retrieves all Stock Exchanges that are currently live in the market")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/live")
    public ResponseEntity<ApiRespond> getAllStockExchangesLiveInMarket(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<StockExchangeDto> liveExchanges = stockExchangeService.getAllStockExchangesLiveInMarket(page, size);

        return ResponseEntity.ok(new ApiRespond(
                HttpStatus.OK,
                "Live Stock Exchanges retrieved successfully",
                liveExchanges
        ));
    }

    @Operation(summary = "Get all stocks in A particular StockExchange which A on pages default page size 5", description = "Get all stocks in A particular StockExchange which A on pages default page size 5")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}/stocks")
    public ResponseEntity<ApiRespond> getAllStocksByExchange(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String sortBy) {

        Page<StockDto> stocks = stockExchangeService.getAllStocksByExchange(id, page, size, sortBy);
        return ResponseEntity.ok(new ApiRespond(HttpStatus.OK, "All Available Stocks In StockExchange", stocks));
    }

    @Operation(summary = "Create a new stock exchange", description = "Creates a new stock exchange in the system")
    @ApiResponse(responseCode = "201", description = "Stock exchange created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "409", description = "Stock exchange with same name already exists")
    @PreAuthorize("hasRole('USER')")
    @PostMapping()
    public ResponseEntity<ApiRespond> createStockExchange(
            @Valid @RequestBody StockExchangeCreationRequest request) {

        StockExchangeDto createdStockExchange = stockExchangeService.createStockExchange(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiRespond(
                        HttpStatus.CREATED,
                        "Stock exchange created successfully",
                        createdStockExchange
                ));
    }

    @Operation(summary = "Update a stock exchange", description = "Updates an existing stock exchange in the system")
    @ApiResponse(responseCode = "200", description = "Stock exchange updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "Stock exchange not found")
    @PreAuthorize("hasRole('USER')")
    @PutMapping(value = "/{id}")
    public ResponseEntity<ApiRespond> updateStockExchange(
            @PathVariable @Positive Long id,
            @Valid @RequestBody StockExchangeUpdateRequest request) {

        System.out.println(request);
        StockExchangeDto updatedStockExchange = stockExchangeService.updateStockExchange(id, request);

        return ResponseEntity.ok(new ApiRespond(
                HttpStatus.OK,
                "Stock exchange updated successfully",
                updatedStockExchange
        ));
    }

    @Operation(summary = "Delete a stock exchange", description = "Deletes a stock exchange from the system")
    @ApiResponse(responseCode = "204", description = "Stock exchange deleted successfully")
    @ApiResponse(responseCode = "404", description = "Stock exchange not found")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStockExchange(@PathVariable @Positive long id) {
        stockExchangeService.deleteStockExchange(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add stock to stock exchange", description = "Associates a stock with a stock exchange")
    @ApiResponse(responseCode = "201", description = "Stock added to stock exchange successfully")
    @ApiResponse(responseCode = "404", description = "Stock exchange or stock not found")
    @ApiResponse(responseCode = "409", description = "Stock already exists in this stock exchange")
    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/{stockExchangeId}/stocks/{stockId}")
    public ResponseEntity<ApiRespond> addStockToStockExchange(
            @PathVariable @Positive long stockExchangeId,
            @PathVariable @Positive long stockId) {

        StockListingDto stockListingDto = stockExchangeService.addStockToStockExchange(stockExchangeId, stockId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiRespond(
                        HttpStatus.CREATED,
                        "Stock added successfully to stock exchange",
                        stockListingDto
                ));
    }

    @Operation(summary = "Add multiple stocks to stock exchange", 
              description = "Associates multiple stocks with a stock exchange in a single operation")
    @ApiResponse(responseCode = "201", description = "Stocks added to stock exchange successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "Stock exchange or one or more stocks not found")
    @ApiResponse(responseCode = "409", description = "One or more stocks already exist in this stock exchange")
    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/{stockExchangeId}/stocks")
    public ResponseEntity<ApiRespond> addStocksToStockExchange(
            @PathVariable @Positive long stockExchangeId,
            @Valid @RequestBody AddStocksToExchangeRequest request) {

        List<StockListingDto> stockListingDtos = stockExchangeService.addStocksToStockExchange(
                stockExchangeId, request.getStockIds());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiRespond(
                        HttpStatus.CREATED,
                        String.format("Successfully added %d stocks to stock exchange", stockListingDtos.size()),
                        stockListingDtos
                ));
    }

    @Operation(summary = "Remove multiple stocks from stock exchange", 
              description = "Removes multiple stocks from a stock exchange in a single operation")
    @ApiResponse(responseCode = "200", description = "Stocks removed from stock exchange successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "Stock exchange or one or more stock listings not found")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping(value = "/{stockExchangeId}/stocks")
    public ResponseEntity<ApiRespond> removeStocksFromStockExchange(
            @PathVariable @Positive long stockExchangeId,
            @Valid @RequestBody AddStocksToExchangeRequest request) {
            
        stockExchangeService.removeStocksFromStockExchange(stockExchangeId, request.getStockIds());
        
        return ResponseEntity.ok(new ApiRespond(
                HttpStatus.OK,
                "Stocks removed successfully from stock exchange",
                null
        ));
    }

    @Operation(summary = "Remove stock from stock exchange", description = "Removes a stock from a stock exchange")
    @ApiResponse(responseCode = "204", description = "Stock removed from stock exchange successfully")
    @ApiResponse(responseCode = "404", description = "Stock exchange or stock not found")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{stockExchangeId}/stocks/{stockId}")
    public ResponseEntity<Void> removeStockFromStockExchange(
            @PathVariable @Positive long stockExchangeId,
            @PathVariable @Positive long stockId) {

        stockExchangeService.removeStockFromStockExchange(stockExchangeId, stockId);
        return ResponseEntity.noContent().build();
    }
}
