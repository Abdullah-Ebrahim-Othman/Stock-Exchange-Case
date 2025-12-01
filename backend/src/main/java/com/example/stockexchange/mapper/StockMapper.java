package com.example.stockexchange.mapper;

import com.example.stockexchange.dto.StockDto;
import com.example.stockexchange.entity.Stock;
import com.example.stockexchange.request.StockCreationRequest;
import com.example.stockexchange.request.StockPriceUpdateRequest;
import org.mapstruct.Mapper;
import java.util.List;


@Mapper(componentModel = "spring")
public interface StockMapper {

    Stock map(StockCreationRequest stockCreationRequest);

    StockDto map(Stock stock);

    Stock map(StockPriceUpdateRequest stockPriceUpdateRequest);

    List<StockDto> map(List<Stock> stocks);
}
