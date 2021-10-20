package com.home.project.portfolio.processor;

import com.home.project.portfolio.model.operations.Operations;
import com.home.project.portfolio.model.response.OperationsDto;

@FunctionalInterface
public interface OperationsProcessor {

    void apply(Operations operations, OperationsDto operationsDto);
}
