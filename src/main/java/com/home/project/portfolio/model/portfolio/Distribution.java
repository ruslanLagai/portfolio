package com.home.project.portfolio.model.portfolio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class to represent assets distribution
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Distribution {
    private double totalInStocks;
    private double totalInFunds;
    private double totalInBounds;
    private double totalInCash;
    private double assetsInRub;
    private double assetsInUsd;
}
