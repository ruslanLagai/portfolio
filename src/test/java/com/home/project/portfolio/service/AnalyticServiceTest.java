package com.home.project.portfolio.service;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.analytic.Period;
import com.home.project.portfolio.repository.OperationRepository;
import com.home.project.portfolio.repository.StockRepository;
import com.home.project.portfolio.utils.OperationGroups;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

import static com.home.project.portfolio.service.OperationsServiceTest.ACCOUNT_ID;
import static com.home.project.portfolio.utils.Profiles.TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Class to test {@link AnalyticService}
 */
@DisplayName("Test analytic")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AnalyticServiceTest {

    @Container
    protected static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8");

    static {
        mySQLContainer.start();
    }

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
    }

    @Autowired
    private AnalyticService analyticService;

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private StockRepository stockRepository;


    @Test
    @DisplayName("All time")
    void analyzeAccount() {
        var result = analyticService.analyzeAccount(ACCOUNT_ID, Period.ALL_TIME);

        var savedTickers = stockRepository.findAll();
        var savedOperations = operationRepository.findAll();
        assertAll(() -> {
            assertThat(result.getAnalyticData().size(), Matchers.greaterThan(10));
            assertThat(savedOperations.size(), Matchers.greaterThan(1000));
            result.getAnalyticData().forEach(analyticData -> {
                assertNotNull(analyticData.getTicker());
                assertNotNull(analyticData.getFigi());
                assertNotNull(analyticData.getCurrency());
            });

            assertThat(result.getServiceCommission().size(), Matchers.equalTo(2));
            result.getServiceCommission().forEach(commission ->
                    assertThat(commission.getCommission(), Matchers.greaterThan(0.0)));

            assertThat(result.getPayments().size(), Matchers.greaterThan(2));
            result.getPayments().forEach(payment -> {
                assertThat(payment.getPayment(), Matchers.greaterThan(0.0));
                assertTrue(OperationGroups.PAYMENTS.contains(payment.getOperationType()));
            });

            assertThat(result.getTaxes().size(), Matchers.greaterThan(2));
            result.getTaxes().forEach(taxes -> {
                assertThat(taxes.getCurrency(), Matchers.equalTo(Currency.RUB));
                assertThat(taxes.getTaxes(), Matchers.greaterThan(0.0));
                assertTrue(OperationGroups.TAXES.contains(taxes.getOperationType()));
            });
        });
    }
}