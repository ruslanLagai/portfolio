package com.home.project.portfolio.processor;

import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.model.response.StockDto;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface AnalyticProcessor {
    Map<String, StockDto> apply(MultiValueMap<String, Operation> operations, List<Position> positions);
}
