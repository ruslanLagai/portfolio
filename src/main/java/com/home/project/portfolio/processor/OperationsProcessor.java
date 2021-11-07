package com.home.project.portfolio.processor;

import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.response.OperationsDto;

import java.util.List;

@FunctionalInterface
public interface OperationsProcessor {

    void apply(List<Operation> operations, OperationsDto operationsDto);
}
