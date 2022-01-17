package com.home.project.portfolio.model.portfolio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Class to represent currencies for specific account
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Currencies {
    private String trackingId;
    private String status;
    private Payload payload;

    @Data
    public static class Payload {
        private List<Currency> currencies;
    }
}
