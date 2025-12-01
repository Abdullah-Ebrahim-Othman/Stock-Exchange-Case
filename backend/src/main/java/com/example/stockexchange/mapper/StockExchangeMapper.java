package com.example.stockexchange.mapper;

import com.example.stockexchange.dto.StockExchangeDto;
import com.example.stockexchange.entity.Stock;
import com.example.stockexchange.entity.StockExchange;
import com.example.stockexchange.request.StockExchangeCreationRequest;
import com.example.stockexchange.request.StockExchangeUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockExchangeMapper {

    StockExchange map(StockExchangeCreationRequest stockCreationRequest);

    StockExchangeDto map(StockExchange stockExchange);

    List<StockExchangeDto> map(List<StockExchange> stockExchanges);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void map(StockExchangeUpdateRequest stockExchangeUpdateRequest, @MappingTarget StockExchange stockExchange);

    StockExchange map(StockExchangeUpdateRequest stockExchangeDto);
}