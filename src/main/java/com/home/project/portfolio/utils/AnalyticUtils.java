package com.home.project.portfolio.utils;

import com.home.project.portfolio.model.analytic.AnalyticData;
import com.home.project.portfolio.model.response.AnalyticDto;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Utility class to process portfolio analytics
 */
@Log4j2
public class AnalyticUtils {

    private static final BiFunction<AnalyticData, AnalyticDto, String> processCommission = (data, analyticDto) -> {
        analyticDto.getServiceCommission().add(data.getServiceCommission());
        return null;
    };
    private static final BiFunction<AnalyticData, AnalyticDto, String> processTax = (data, analyticDto) -> {
        analyticDto.getTaxes().addAll(data.getTaxes());
        return null;
    };
    private static final BiFunction<AnalyticData, AnalyticDto, String> processPayment = (data, analyticDto) -> {
        data.getPayment().forEach(payment ->
                analyticDto.getPayments().stream()
                        .filter(p -> p.getCurrency().equals(payment.getCurrency()))
                        .filter(p -> p.getOperationType().equals(payment.getOperationType()))
                        .findFirst()
                        .ifPresentOrElse(p -> p.setPayment(p.getPayment() + payment.getPayment()),
                                () -> analyticDto.getPayments().add(payment)));
        return null;
    };
    private static final BiFunction<AnalyticData, Map<String, AnalyticData>, String> processTradeOperation = (data, map) -> {
        if (!map.containsKey(data.getTicker())) {
            var toPut = AnalyticData.builder()
                    .commission(data.isCommission() ? data.getCommission() : 0.0)
                    .revenue(data.isRevenue() ? data.getRevenue() : 0.0)
                    .ticker(data.getTicker())
                    .instrumentType(data.getInstrumentType())
                    .currency(data.getCurrency())
                    .figi(data.getFigi())
                    .build();
            map.put(data.getTicker(), toPut);
        } else {
            var toUpdate = map.get(data.getTicker());
            if (data.isRevenue()) {
                toUpdate.setRevenue(data.getRevenue());
            } else if (data.isCommission()) {
                toUpdate.setCommission(data.getCommission());
            }
        }

        return null;
    };

    public static AnalyticDto mergeAnalyticData(List<AnalyticData> dataList) {
        var analyticDto = new AnalyticDto();
        Map<String, AnalyticData> map = new HashMap<>();
        dataList.forEach(data -> {
            var nonTradingProcessor = getNonTradingProcessor(data);
            if (nonTradingProcessor != null) {
                nonTradingProcessor.apply(data, analyticDto);
            } else {
                processTradeOperation.apply(data, map);
            }
        });
        analyticDto.setAnalyticData(new ArrayList<>(map.values()));
        return analyticDto;
    }

    private static BiFunction<AnalyticData, AnalyticDto, String> getNonTradingProcessor(AnalyticData data) {
        return data.isServiceCommission() ? processCommission
                : data.isTaxes() ? processTax
                : data.isPayment() ? processPayment
                : null;
    }
}
