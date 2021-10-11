package com.home.project.portfolio.model.portfolio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Class to represent tinkoff portfolio indo
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Accounts {
    private String trackingId;
    private Payload payload;
    private String status;

    /**
     * Class to represent tinkoff portfolio payload
     */
    @Data
    public static class Payload {
        private List<Account> accounts;
    }
}
