package com.home.project.portfolio.processor;

import com.home.project.portfolio.helpers.TestUtils;
import com.home.project.portfolio.model.operations.Operations;
import com.home.project.portfolio.model.portfolio.Position;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractProcessorTest {

    protected static final String AAPL = "AAPL";
    protected static final String SBERP = "SBERP";
    protected static final String AMZN = "AMZN";
    protected static final String ALEXION = "ALEXION";
    protected static final Operations OPERATIONS = TestUtils.readOperations();



    protected static final List<Position> AAPL_POSITIONS = TestUtils.readPositions()
            .getPayload().getPositions().stream()
            .filter(position -> position.getTicker().equals(AAPL))
            .collect(Collectors.toList());

    protected static final List<Position> AAPL_AMZN_POSITIONS = TestUtils.readPositions()
            .getPayload().getPositions().stream()
            .filter(position -> position.getTicker().equals(AAPL) || position.getTicker().equals(AMZN))
            .collect(Collectors.toList());
}
