package com.home.project.portfolio.model.operations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Overbook {
    private String figi;
    private int depth;
    private List<OrderResponse> bids;
    private List<OrderResponse> asks;
    private StockAvailability tradeStatus;
    private double minPriceIncrement;
    private double faceValue;
    private double lastPrice;
    private double closePrice;
    private double limitUp;
    private double limitDown;


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderResponse {
        private double price;
        private int quantity;
    }
}
