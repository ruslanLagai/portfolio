package com.home.project.portfolio.model.portfolio;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

/**
 * Stocks distribution by sectors
 *
 * @author rlagay
 */
@Data
@Builder
public class Sector {
    private String sector;
    @JsonIgnore
    private double value;
    private double sectorWeight;
}
