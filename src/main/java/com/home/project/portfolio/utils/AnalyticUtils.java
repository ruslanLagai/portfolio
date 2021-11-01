package com.home.project.portfolio.utils;

import com.home.project.portfolio.model.analytic.AnalyticData;
import com.home.project.portfolio.model.response.AnalyticDto;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to process portfolio analytics
 */
@Log4j2
public class AnalyticUtils {

    public static AnalyticDto mergeAnalyticData(List<AnalyticData> dataList) {
        var analyticDto = new AnalyticDto();
        List<AnalyticData> analyticDtoList = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();
        dataList.forEach(data -> {
            if (data.getServiceCommission() != null) {
                analyticDto.getServiceCommission().add(data.getServiceCommission());
            } else if (!map.containsKey(data.getTicker())) {
                var toPut = AnalyticData.builder()
                        .commission(data.isCommission() ? data.getCommission() : 0.0)
                        .revenue(data.isRevenue() ? data.getRevenue() : 0.0)
                        .ticker(data.getTicker())
                        .currency(data.getCurrency())
                        .figi(data.getFigi())
                        .build();
                analyticDtoList.add(toPut);
                map.put(data.getTicker(), analyticDtoList.indexOf(toPut));
            } else if (map.containsKey(data.getTicker())) {
                var toUpdate = analyticDtoList.get(map.get(data.getTicker()));
                if (data.isRevenue()) {
                    toUpdate.setRevenue(data.getRevenue());
                } else if (data.isCommission()) {
                    toUpdate.setCommission(data.getCommission());
                }
            }
        });
        analyticDto.setAnalyticData(analyticDtoList);
        return analyticDto;
    }
}
