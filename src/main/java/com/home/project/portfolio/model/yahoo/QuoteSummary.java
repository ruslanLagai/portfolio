package com.home.project.portfolio.model.yahoo;

import lombok.Data;

import java.util.List;

/**
 * @author rlagay
 */
@Data
public class QuoteSummary {
    private String error;
    private List<Result> result;

    @Data
    public static class Result {
        private CompanyOverview assetProfile;
    }
}
