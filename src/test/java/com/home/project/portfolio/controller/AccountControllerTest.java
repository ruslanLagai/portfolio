package com.home.project.portfolio.controller;

import com.home.project.portfolio.model.portfolio.Account;
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

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test for {@link AccountController}
 */
@DisplayName("Test account controller")
@WebMvcTest(value = AccountController.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortfolioService portfolioService;

    @SneakyThrows
    @Test
    @DisplayName("basic test")
    void getAccounts() {
        when(portfolioService.getAccounts()).thenReturn(mockDto());
        mockMvc.perform(get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts").exists())
                .andExpect(jsonPath("$.accounts[0].brokerAccountType").value(Matchers.equalTo("Tinkoff")))
                .andExpect(jsonPath("$.accounts[0].brokerAccountId").value(Matchers.equalTo("123")))
                .andExpect(jsonPath("$.accounts[1].brokerAccountType").value(Matchers.equalTo("TinkoffIis")))
                .andExpect(jsonPath("$.accounts[1].brokerAccountId").value(Matchers.equalTo("1234")));
    }

    @SneakyThrows
    @Test
    @DisplayName("Feign exception test")
    void testFeignGetStocks() {
        when(portfolioService.getAccounts()).thenThrow(new ApiRuntimeException("", "", new RuntimeException(), ""));

        mockMvc.perform(get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway());
    }

    private List<Account> mockDto() {
        var account1 = new Account();
        account1.setBrokerAccountId("123");
        account1.setBrokerAccountType(Account.AccountType.Tinkoff);

        var account2 = new Account();
        account2.setBrokerAccountId("1234");
        account2.setBrokerAccountType(Account.AccountType.TinkoffIis);
        return List.of(account1, account2);
    }

}