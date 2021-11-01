package com.home.project.portfolio.calculation;

import com.home.project.portfolio.model.operations.Operation;

import java.util.List;

public interface Calculator {

    double calculate(List<Operation> operations);
}
