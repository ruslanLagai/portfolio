package com.home.project.portfolio.controller;

import com.home.project.portfolio.helpers.TestUtils;
import com.home.project.portfolio.model.portfolio.Portfolio;
import com.home.project.portfolio.model.portfolio.Sector;
import com.home.project.portfolio.model.response.PortfolioDto;
import com.home.project.portfolio.service.PortfolioService;
import com.home.project.portfolio.service.StockSectorService;
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

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test for {@link StocksSectorController}
 * @author rlagay
 */
@DisplayName("Test sector controller")
@WebMvcTest(value = StocksSectorController.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class StocksSectorControllerTest {

    private final Portfolio portfolio = TestUtils.readPositions();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortfolioService portfolioService;

    @MockBean
    private StockSectorService stockSectorService;

    @SneakyThrows
    @Test
    @DisplayName("basic test")
    void getStockSectors() {
        when(portfolioService.getPortfolio(any())).thenReturn(new PortfolioDto());
        when(stockSectorService.getSectorData(any())).thenReturn(mockDto());

        mockMvc.perform(get("/sectors?accountId=1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectors").exists())
                .andExpect(jsonPath("$.sectors[0].sector").value(Matchers.equalTo("sector")))
                .andExpect(jsonPath("$.sectors[0].sectorWeight").value(Matchers.equalTo(10.11)))
                .andExpect(jsonPath("$.sectors[1].sector").value(Matchers.equalTo("sector1")))
                .andExpect(jsonPath("$.sectors[1].sectorWeight").value(Matchers.equalTo(0.0)));
    }

    @SneakyThrows
    @Test
    @DisplayName("Unauthorized test")
    void getStockSectorsUnauthorized() {
        when(portfolioService.getPositionsForAccount(any())).thenThrow(new FeignException.Unauthorized("",
                Request.create(Request.HttpMethod.GET, "url", Map.of(), null, new RequestTemplate()), null));

        mockMvc.perform(get("/sectors?accountId=1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @SneakyThrows
    @Test
    @DisplayName("Feign exception test")
    void getStockSectorsFeignException() {
        when(portfolioService.getPositionsForAccount(any())).thenThrow(new FeignException.InternalServerError("",
                Request.create(Request.HttpMethod.GET, "url", Map.of(), null, new RequestTemplate()), null));

        mockMvc.perform(get("/sectors?accountId=1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway());
    }

    private List<Sector> mockDto() {
        return List.of(Sector.builder().sector("sector").sectorWeight(10.11).build(),
                Sector.builder().sector("sector1").sectorWeight(0.0).build());
    }
}