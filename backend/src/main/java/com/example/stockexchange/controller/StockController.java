package com.example.stockexchange.controller;

import com.example.stockexchange.dto.StockDto;
import com.example.stockexchange.dto.StockExchangeDto;
import com.example.stockexchange.request.StockCreationRequest;
import com.example.stockexchange.request.StockPriceUpdateRequest;
import com.example.stockexchange.response.ApiRespond;
import com.example.stockexchange.service.StockService;
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

@RequestMapping("${app.paths.api-base}${app.paths.api-version}/stock")
@RequiredArgsConstructor
@Validated
@RestController
@Tag(name = "Stock Rest API Endpoints", description = "Operations related to stocks in the system")
public class StockController {

    private final StockService stockService;

    @Operation(summary = "Get stock by ID", description = "Retrieves a single stock by its ID")
    @ApiResponse(responseCode = "200", description = "Stock found and returned")
    @ApiResponse(responseCode = "404", description = "Stock not found")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiRespond> getStockById(@PathVariable @Positive long id) {
        StockDto stock = stockService.getStockById(id);
        return ResponseEntity.ok(new ApiRespond(
                HttpStatus.OK,
                "Stock retrieved successfully",
                stock
        ));
    }

    @Operation(summary = "Get all Stocks", description = "Retrieves a paginated list of all Stocks")
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<ApiRespond> getAllStocks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Page<StockDto> stocks = stockService.getAllStocks(page, size, sortBy, direction);

        return ResponseEntity.ok(new ApiRespond(
                HttpStatus.OK,
                "Stocks retrieved successfully",
                stocks
        ));
    }

    @Operation(summary = "Get all Stock Exchanges for a Stock",
            description = "Retrieves all Stock Exchanges where a specific stock is listed")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/stocks/{stockId}/exchanges")
    public ResponseEntity<ApiRespond> getAllStockExchangesByStock(
            @PathVariable Long stockId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<StockExchangeDto> stockExchanges = stockService.getAllStockExchangesByStock(stockId, page, size);

        return ResponseEntity.ok(new ApiRespond(
                HttpStatus.OK,
                "Stock Exchanges retrieved successfully",
                stockExchanges
        ));
    }

    @Operation(summary = "Create a new Stock", description = "Creates a new Stock in the system")
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ApiRespond> createStock(@Valid @RequestBody StockCreationRequest stockCreationRequest) {

        StockDto createdStock = stockService.createStock(stockCreationRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiRespond(
                        HttpStatus.CREATED,
                        "Stock created successfully",
                        createdStock
                ));
    }

    @Operation(summary = "Update stock price", description = "Updates the price of an existing stock")
    @ApiResponse(responseCode = "200", description = "Stock price updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "Stock not found")
    @PreAuthorize("hasRole('USER')")
    @PutMapping(value = "/{id}/price")
    public ResponseEntity<ApiRespond> updateStockPrice(
            @PathVariable @Positive long id,
            @Valid @RequestBody StockPriceUpdateRequest request) {

        StockDto updatedStock = stockService.updatePrice(id, request);

        return ResponseEntity.ok(new ApiRespond(
                HttpStatus.OK,
                "Stock price updated successfully",
                updatedStock
        ));
    }

    @Operation(summary = "Delete a stock", description = "Deletes a stock from the system")
    @ApiResponse(responseCode = "204", description = "Stock deleted successfully")
    @ApiResponse(responseCode = "404", description = "Stock not found")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable @Positive long id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }
}
