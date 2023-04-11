package com.home.project.portfolio.processor;

import com.home.project.portfolio.helpers.TestUtils;

import java.util.List;

public abstract class AbstractProcessorTest {

    protected static final String VKCO = "VKCO";
    protected static final String SBER = "SBER";
    protected static final String NLMK = "NLMK";
    protected static final String AMZN = "AMZN";
    protected static final String HUMANA = "HUM";
    protected static final String VLO = "VLO";

    protected static final List<ru.tinkoff.piapi.contract.v1.Operation> OPERATIONS = TestUtils.operations("classpath:testData/get-operations.json");

}
