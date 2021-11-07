package com.home.project.portfolio.model.response;

import com.home.project.portfolio.model.analytic.AnalyticData;
import com.home.project.portfolio.model.analytic.Payments;
import com.home.project.portfolio.model.analytic.ServiceCommission;
import com.home.project.portfolio.model.analytic.Taxes;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AnalyticDto {
    private List<AnalyticData> analyticData = new ArrayList<>();
    private List<ServiceCommission> serviceCommission = new ArrayList<>();
    private List<Taxes> taxes = new ArrayList<>();
    private List<Payments> payments = new ArrayList<>();

    public void addData(AnalyticData data) {
        analyticData.add(data);
    }
}
