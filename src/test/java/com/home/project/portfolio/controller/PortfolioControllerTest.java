package com.home.project.portfolio.controller;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.InstrumentType;
import com.home.project.portfolio.model.operations.Overbook;
import com.home.project.portfolio.model.portfolio.AveragePositionItem;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.model.response.PortfolioDto;
import com.home.project.portfolio.service.PortfolioService;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
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

import java.util.Arrays;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author rlagay
 */
@DisplayName("Test portfolio controller")
@WebMvcTest(value = PortfolioController.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortfolioService portfolioService;

    @SneakyThrows
    @Test
    @DisplayName("basic test")
    void getStocks() {
        when(portfolioService.getPortfolio()).thenReturn(mockDto());
        mockMvc.perform(get("/portfolio")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.positions").exists())
                .andExpect(jsonPath("$.positions.iis[0].ticker").value(Matchers.equalTo("t1")))
                .andExpect(jsonPath("$.positions.iis[0].figi").value(Matchers.equalTo("f1")))
                .andExpect(jsonPath("$.positions.iis[0].instrumentType").value(Matchers.equalTo("STOCK")))
                .andExpect(jsonPath("$.positions.iis[0].balance").value(Matchers.equalTo(10.0)))
                .andExpect(jsonPath("$.positions.iis[0].lots").value(Matchers.equalTo(1)))
                .andExpect(jsonPath("$.positions.iis[0].name").value(Matchers.equalTo("n1")))
                .andExpect(jsonPath("$.positions.iis[0].averagePositionPrice.value").value(Matchers.equalTo(3.0)))
                .andExpect(jsonPath("$.positions.iis[0].averagePositionPrice.currency").value(Matchers.equalTo("USD")))
                .andExpect(jsonPath("$.prices.f1[0].figi").value(Matchers.equalTo("f1")))
                .andExpect(jsonPath("$.prices.f1[0].closePrice").value(Matchers.equalTo(34.1)))
                .andExpect(jsonPath("$.prices.f1[0].lastPrice").value(Matchers.equalTo(33.0)));
    }

    @SneakyThrows
    @Test
    @DisplayName("Unauthorized test")
    void testGetStocks() {
        when(portfolioService.getPortfolio()).thenThrow(new FeignException.Unauthorized("",
                Request.create(Request.HttpMethod.GET, "url", Map.of(), null, new RequestTemplate()), null));

        mockMvc.perform(get("/portfolio")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @SneakyThrows
    @Test
    @DisplayName("Feign exception test")
    void testFeignGetStocks() {
        when(portfolioService.getPortfolio()).thenThrow(new FeignException.InternalServerError("",
                Request.create(Request.HttpMethod.GET, "url", Map.of(), null, new RequestTemplate()), null));

        mockMvc.perform(get("/portfolio")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway());
    }

    private PortfolioDto mockDto() {
        var dto = new PortfolioDto();
        dto.addPositions("iis", Arrays.asList(
            mockPosition("t1", "f1"), mockPosition("t2", "f2")
        ));
        var overbook = new Overbook();
        overbook.setFigi("f1");
        overbook.setClosePrice(34.1);
        overbook.setLastPrice(33.0);
        dto.addPrice("f1", overbook);
        return dto;
    }

    private Position mockPosition(String ticker, String figi) {
        var position = new Position();
        var avg = new AveragePositionItem();
        avg.setValue(3.0);
        avg.setCurrency(Currency.USD);
        position.setName("n1");
        position.setTicker(ticker);
        position.setFigi(figi);
        position.setBalance(10.0);
        position.setLots(1);
        position.setInstrumentType(InstrumentType.STOCK);
        position.setAveragePositionPrice(avg);
        return position;
    }
}