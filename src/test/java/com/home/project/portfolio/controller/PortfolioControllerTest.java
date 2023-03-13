package com.home.project.portfolio.controller;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.InstrumentType;
import com.home.project.portfolio.model.operations.Overbook;
import com.home.project.portfolio.model.portfolio.AveragePositionItem;
import com.home.project.portfolio.model.portfolio.Distribution;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.model.response.PortfolioDto;
import com.home.project.portfolio.service.PortfolioService;
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
import ru.tinkoff.piapi.core.exception.ApiRuntimeException;

import java.util.Arrays;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
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
        when(portfolioService.getPortfolio(any())).thenReturn(mockDto());
        mockMvc.perform(get("/portfolio")
                        .param("accountId", "123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.positions").exists())
                .andExpect(jsonPath("$.positions.[0].ticker").value(Matchers.equalTo("t1")))
                .andExpect(jsonPath("$.positions.[0].figi").value(Matchers.equalTo("f1")))
                .andExpect(jsonPath("$.positions.[0].instrumentType").value(Matchers.equalTo("STOCK")))
                .andExpect(jsonPath("$.positions.[0].balance").value(Matchers.equalTo(10.0)))
                .andExpect(jsonPath("$.positions.[0].lots").value(Matchers.equalTo(1)))
                .andExpect(jsonPath("$.positions.[0].name").value(Matchers.equalTo("n1")))
                .andExpect(jsonPath("$.positions.[0].averagePositionPrice.value").value(Matchers.equalTo(3.0)))
                .andExpect(jsonPath("$.positions.[0].averagePositionPrice.currency").value(Matchers.equalTo("USD")))
                .andExpect(jsonPath("$.prices.f1.figi").value(Matchers.equalTo("f1")))
                .andExpect(jsonPath("$.prices.f1.lastPrice").value(Matchers.equalTo(33.0)))
                .andExpect(jsonPath("$.distribution.totalInFunds").value(Matchers.equalTo(1.0)))
                .andExpect(jsonPath("$.distribution.totalInStocks").value(Matchers.equalTo(1.0)))
                .andExpect(jsonPath("$.distribution.totalInBounds").value(Matchers.equalTo(1.0)))
                .andExpect(jsonPath("$.distribution.assetsInUsd").value(Matchers.equalTo(10.0)))
                .andExpect(jsonPath("$.distribution.assetsInRub").value(Matchers.equalTo(10.0)))
                .andExpect(jsonPath("$.cash.USD.balance").value(Matchers.equalTo(10.0)))
                .andExpect(jsonPath("$.cash.RUB.balance").value(Matchers.equalTo(10.0)))
                .andExpect(jsonPath("$.cash.USD.averagePrice").value(Matchers.equalTo(10.0)))
                .andExpect(jsonPath("$.cash.RUB.averagePrice").value(Matchers.equalTo(10.0)))
                .andExpect(jsonPath("$.cash.USD.currentPrice").value(Matchers.equalTo(10.0)))
                .andExpect(jsonPath("$.cash.RUB.currentPrice").value(Matchers.equalTo(10.0)));

    }

    @SneakyThrows
    @Test
    @DisplayName("Api exception test")
    void testFeignGetStocks() {
        when(portfolioService.getPortfolio(any())).thenThrow(new ApiRuntimeException("", "", new RuntimeException(), ""));

        mockMvc.perform(get("/portfolio")
                        .param("accountId", "123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway());
    }

    private PortfolioDto mockDto() {
        var dto = new PortfolioDto();
        dto.getPositions().addAll(Arrays.asList(
            mockPosition("t1", "f1"), mockPosition("t2", "f2")
        ));
        var overbook = new Overbook();
        overbook.setFigi("f1");
        overbook.setLastPrice(33.0);

        var distribution = Distribution.builder()
                .totalInFunds(1.0)
                .totalInStocks(1.0)
                .totalInBounds(1.0)
                .assetsInUsd(10.0)
                .assetsInRub(10.0)
                .build();

        dto.getPrices().put("f1", overbook);
        dto.setDistribution(distribution);

        dto.setCash(Map.of(
                Currency.USD, PortfolioDto.CurrencyDto.builder()
                        .balance(10.0)
                        .averagePrice(10.0)
                        .currentPrice(10.0)
                        .build(),
                Currency.RUB, PortfolioDto.CurrencyDto.builder()
                        .balance(10.0)
                        .averagePrice(10.0)
                        .currentPrice(10.0)
                        .build()
        ));
        return dto;
    }

    private Position mockPosition(String ticker, String figi) {
        var position = new Position();
        var avg = new AveragePositionItem(Currency.USD, 3.0);
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