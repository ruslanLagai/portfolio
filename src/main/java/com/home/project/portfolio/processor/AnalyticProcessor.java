package com.home.project.portfolio.processor;

import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.model.analytic.AnalyticData;
import org.springframework.util.MultiValueMap;

import java.util.List;

@FunctionalInterface
public interface AnalyticProcessor {
    List<AnalyticData> apply(MultiValueMap<String, Operation> operations, List<Position> positions);
}
