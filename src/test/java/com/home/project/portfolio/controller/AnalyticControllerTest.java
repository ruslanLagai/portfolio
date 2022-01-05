package com.home.project.portfolio.controller;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.analytic.AnalyticData;
import com.home.project.portfolio.model.analytic.Payment;
import com.home.project.portfolio.model.analytic.ServiceCommission;
import com.home.project.portfolio.model.analytic.Taxes;
import com.home.project.portfolio.model.operations.OperationType;
import com.home.project.portfolio.model.response.AnalyticDto;
import com.home.project.portfolio.service.AnalyticService;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author rlagay
 */
@DisplayName("Test analytic controller")
@WebMvcTest(AnalyticController.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class AnalyticControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticService analyticService;

    @BeforeEach
    public void init() {
        when(analyticService.analyzeAccount(any(), any())).thenReturn(mockAnalyticDto());
    }

    @SneakyThrows
    @Test
    @DisplayName("Basic test")
    void getAnalyticFor() {
        mockMvc.perform(get("/analytic")
                        .param("accountId", "123")
                        .param("period", "day")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analyticData").exists())
                .andExpect(jsonPath("$.analyticData[0].ticker").value(Matchers.equalTo("t")))
                .andExpect(jsonPath("$.analyticData[0].revenue").value(Matchers.equalTo(10.0)))
                .andExpect(jsonPath("$.analyticData[0].commission").value(Matchers.equalTo(10.0)))
                .andExpect(jsonPath("$.serviceCommission").exists())
                .andExpect(jsonPath("$.serviceCommission[0].currency").value(Matchers.equalTo("USD")))
                .andExpect(jsonPath("$.serviceCommission[0].commission").value(Matchers.equalTo(10.0)))
                .andExpect(jsonPath("$.taxes").exists())
                .andExpect(jsonPath("$.taxes[0].currency").value(Matchers.equalTo("RUB")))
                .andExpect(jsonPath("$.taxes[0].operationType").value(Matchers.equalTo("TAX")))
                .andExpect(jsonPath("$.payments").exists())
                .andExpect(jsonPath("$.payments[0].payment").value(Matchers.equalTo(10.0)))
                .andExpect(jsonPath("$.payments[0].currency").value(Matchers.equalTo("USD")))
                .andExpect(jsonPath("$.payments[0].operationType").value(Matchers.equalTo("PAY_IN")));
    }

    private AnalyticDto mockAnalyticDto() {
        var dto = new AnalyticDto();
        dto.setPayments(List.of(Payment.builder()
                .payment(10.0)
                .currency(Currency.USD)
                .operationType(OperationType.PAY_IN).build()));
        dto.setTaxes(List.of(Taxes.builder().taxes(10.0).currency(Currency.RUB).operationType(OperationType.TAX).build()));
        dto.setServiceCommission(List.of(ServiceCommission.builder().commission(10.0).currency(Currency.USD).build()));
        dto.setAnalyticData(List.of(AnalyticData.builder().ticker("t").revenue(10.0).commission(10.0).build()));
        return dto;
    }
}