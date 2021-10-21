package com.home.project.portfolio.model.operations;

import lombok.Data;

@Data
public class PriceResponse {
    private String trackingId;
    private String status;
    private Overbook payload;
}
