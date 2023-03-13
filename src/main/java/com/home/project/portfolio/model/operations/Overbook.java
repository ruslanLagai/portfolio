package com.home.project.portfolio.model.operations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Overbook {
    private String figi;
    private SecurityTradingStatus tradeStatus;
    private double lastPrice;


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderResponse {
        private double price;
        private int quantity;
    }
}
