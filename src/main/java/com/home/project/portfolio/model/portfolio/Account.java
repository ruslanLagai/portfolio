package com.home.project.portfolio.model.portfolio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Class to represent tinkoff account info
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {
    private AccountType brokerAccountType;
    private String brokerAccountId;
    private String status;
    private String name;

    public enum AccountType {
        Tinkoff,
        TinkoffIis,
        Unspecified,
        InvestBox
    }
}
