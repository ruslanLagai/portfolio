package com.home.project.portfolio.service;

import com.home.project.portfolio.helpers.TestUtils;
import com.home.project.portfolio.model.InstrumentType;
import com.home.project.portfolio.model.portfolio.Account;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tinkoff.piapi.contract.v1.GetOrderBookResponse;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.UsersService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.home.project.portfolio.utils.Constants.CURRENCY_FIGI_MAP;
import static com.home.project.portfolio.utils.TestData.BLOCKED_FIGIS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Class to test {@link PortfolioService}
 */
@DisplayName("Test Tinkoff service")
@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = AbstractDbTest.Initializer.class)
class PortfolioServiceTest extends AbstractDbTest {

    private static final String ACCOUNT_ONE = "classpath:testData/account-one.json";
    private static final String ACCOUNT_TWO = "classpath:testData/account-two.json";

    private static final String PORTFOLIO_RESOURCE = "classpath:testData/get-portfolio.json";
    private static final String POSITIONS_RESOURCE = "classpath:testData/get-positions.json";
    private static final String LAST_PRICES_RESOURCE = "classpath:testData/get-last-prices.json";
    private static final String CURRENCY_PRICES_RESOURCE = "classpath:testData/get-currency-prices.json";

    private static final List<String> FIGIS = List.of("US0900401060", "BBG0077VNXV6", "US19260Q1076", "BBG004S681B4",
        "BBG006DZTJ56", "BBG004731032", "BBG004730N88", "BBG004RVFFC0", "BBG00HTN2CQ3", "US4234031049");

    @MockBean
    private UsersService usersService;

    @MockBean
    private InstrumentsService instrumentsService;

    @MockBean
    private MarketDataService marketDataService;

    @MockBean
    private ru.tinkoff.piapi.core.OperationsService operationsService;

    @Autowired
    PortfolioService portfolioService;

    @Test
    @DisplayName("Test get accounts")
    void getAccounts() {
        when(usersService.getAccountsSync()).thenReturn(List.of(TestUtils.account(ACCOUNT_ONE), TestUtils.account(ACCOUNT_TWO)));
        var result = portfolioService.getAccounts();

        assertThat(result.size(), Matchers.equalTo(1));

        assertAll(() -> {
            assertEquals("ACCOUNT_STATUS_OPEN", result.get(0).getStatus());
            assertEquals("111111111111", result.get(0).getBrokerAccountId());
            assertEquals("Брокерский счёт", result.get(0).getName());
            assertEquals(Account.AccountType.Tinkoff, result.get(0).getBrokerAccountType());
        });
    }

    @Test
    @DisplayName("test get portfolio")
    void getPortfolio() {
        when(operationsService.getPositions("1111111111111"))
            .thenReturn(CompletableFuture.completedFuture(TestUtils.positions(POSITIONS_RESOURCE)));
        when(operationsService.getPortfolioSync("1111111111111"))
            .thenReturn(TestUtils.portfolio(PORTFOLIO_RESOURCE));
        when(marketDataService.getLastPricesSync(eq(FIGIS)))
            .thenReturn(TestUtils.lastPrices(LAST_PRICES_RESOURCE));
        when(marketDataService.getLastPricesSync(eq(CURRENCY_FIGI_MAP.values())))
            .thenReturn(TestUtils.lastPrices(CURRENCY_PRICES_RESOURCE));
        when(marketDataService.getOrderBookSync(any(), anyInt()))
            .thenReturn(GetOrderBookResponse.newBuilder().setLastPrice(Quotation.newBuilder().setUnits(1).build()).build());
        when(instrumentsService.getInstrumentByFigiSync(any())).thenReturn(Instrument.newBuilder().setFigi("figi")
            .setTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_DEALER_NORMAL_TRADING).build());

        var result = portfolioService.getPortfolio("1111111111111");
        assertAll(() -> {
            assertThat(result.getPositions().size(), Matchers.equalTo(10));
            result.getPositions().forEach(position -> {
                assertEquals(InstrumentType.STOCK, position.getInstrumentType());
                assertNotNull(position.getAveragePositionPrice());
                assertNotNull(position.getExpectedYield());
                assertTrue(StringUtils.isNotBlank(position.getFigi()));
                if (!BLOCKED_FIGIS.contains(position.getFigi())) {
                    assertNotEquals(0.0, position.getBalance());
                }
            });
        });

        //prices
        assertAll(() -> {
            assertThat(result.getPositions().size(), Matchers.equalTo(10));
            result.getPrices().forEach((k, v) -> {
                if (!BLOCKED_FIGIS.contains(k)) {
                    assertThat(v.getLastPrice(), Matchers.greaterThan(0.0));
                }
            });
        });

        // cash
        assertAll(() -> {
            assertEquals(4, result.getCash().size());
            result.getCash().forEach((currency, value) -> {
                assertFalse(result.getCash().isEmpty());
                assertThat(value.getBalance(), Matchers.greaterThan(0.0));
                assertThat(value.getAveragePrice(), Matchers.greaterThanOrEqualTo(0.0));
                assertThat(value.getCurrentPrice(), Matchers.greaterThan(0.0));
            });
        });


        // distribution
        assertAll(() -> {
            assertThat(result.getDistribution().getAssetsInRub(), Matchers.lessThan(0.0));
            assertThat(result.getDistribution().getAssetsInUsd(), Matchers.greaterThan(result.getDistribution().getAssetsInRub()));
            assertThat(result.getDistribution().getTotalInStocks(), Matchers.lessThan(0.0));
            assertThat(result.getDistribution().getTotalInCash(), Matchers.greaterThan(0.0));

            assertThat(result.getDistribution().getTotalInFunds(), Matchers.equalTo(0.0));
            assertThat(result.getDistribution().getTotalInBounds(), Matchers.equalTo(0.0));
        });
    }

    @DisplayName("test get positions for account")
    @Test
    public void testGetPositions() {
        when(operationsService.getPortfolioSync("1111111111111"))
            .thenReturn(TestUtils.portfolio(PORTFOLIO_RESOURCE));
        when(instrumentsService.getInstrumentByFigiSync(any())).thenReturn(Instrument.newBuilder().setFigi("figi")
            .setTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_DEALER_NORMAL_TRADING).build());
        when(operationsService.getPositionsSync("1111111111111"))
            .thenReturn(TestUtils.positions(POSITIONS_RESOURCE));

        var result = portfolioService.getPositionsForAccount("1111111111111");

        assertAll(() -> {
            assertEquals(14, result.size());
            result.forEach(position -> {
                assertFalse(position.getFigi().isEmpty());
                if (!position.getFigi().equals("BBG004S681B4")) {
                    assertNotEquals(0.0, position.getBalance());
                }
                assertNotNull(position.getAveragePositionPrice());
                assertNotNull(position.getExpectedYield());
            });
        });

    }

}