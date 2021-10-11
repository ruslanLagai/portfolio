package com.home.project.portfolio.model.portfolio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Class to represent tinkoff portfolio for specific account
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Portfolio {
    private String trackingId;
    private String status;
    private Payload payload;

    @Data
    public static class Payload {
        private List<Positions> positions;
    }
}
