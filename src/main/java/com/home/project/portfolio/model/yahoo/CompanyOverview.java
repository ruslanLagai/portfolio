package com.home.project.portfolio.model.yahoo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @author rlagay
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyOverview {
    private String country;
    private String sector;
    private String industry;
}
