package com.home.project.portfolio.service;

import com.home.project.portfolio.helpers.TestUtils;
import com.home.project.portfolio.mapper.OperationMapper;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.repository.OperationRepository;
import com.home.project.portfolio.repository.StockRepository;
import com.home.project.portfolio.utils.OperationGroups;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.OperationsService;

import java.time.LocalDate;
import java.util.List;

import static com.home.project.portfolio.service.OperationsServiceTest.ACCOUNT_ID;
import static com.home.project.portfolio.utils.Profiles.TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Class to test {@link AnalyticService}
 */
@Disabled(value = "Integration test with a real service id")
@DisplayName("Test analytic")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AnalyticServiceTest {

    private static final String POSITIONS_RESOURCE = "classpath:testData/get-positions.json";
    private static final String PORTFOLIO_RESOURCE = "classpath:testData/get-portfolio.json";

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

    @Autowired
    private OperationMapper operationMapper;

    @MockBean
    private OperationsService operationsService;

    @MockBean
    private InstrumentsService instrumentsService;

    private final List<Operation> restOperations = TestUtils.operations("classpath:testData/get-operations.json");

    @Test
    @DisplayName("All time")
    void analyzeAccount() {
        when(operationsService.getPositionsSync(ACCOUNT_ID)).thenReturn(TestUtils.positions(POSITIONS_RESOURCE));
        when(operationsService.getExecutedOperationsSync(eq(ACCOUNT_ID), any(), any())).thenReturn(restOperations);
        when(operationsService.getPortfolioSync(ACCOUNT_ID)).thenReturn(TestUtils.portfolio(PORTFOLIO_RESOURCE));
        when(instrumentsService.getInstrumentByFigiSync(eq("US4234031049")))
            .thenReturn(Instrument.newBuilder().setIsin("US4234031049").setTicker("US4234031049").setInstrumentType("share").setName("Hello Group").build());
        when(instrumentsService.getInstrumentByFigiSync(eq("BBG00HTN2CQ3")))
            .thenReturn(Instrument.newBuilder().setIsin("US4234031049").setTicker("SPCE").setInstrumentType("share").setName("Virgin Galactic").build());
        when(instrumentsService.getInstrumentByFigiSync(eq("BBG004RVFFC0")))
            .thenReturn(Instrument.newBuilder().setIsin("RU0009033591").setTicker("TATN").setInstrumentType("share").setName("Татнефть").build());
        when(instrumentsService.getInstrumentByFigiSync(eq("BBG004730N88")))
            .thenReturn(Instrument.newBuilder().setIsin("RU0009029540").setTicker("SBER").setInstrumentType("share").setName("SBER bank").build());
        when(instrumentsService.getInstrumentByFigiSync(eq("BBG004731032")))
            .thenReturn(Instrument.newBuilder().setIsin("RU0009024277").setTicker("LKOH").setInstrumentType("share").setName("Лукойл").build());
        when(instrumentsService.getInstrumentByFigiSync(eq("BBG006DZTJ56")))
            .thenReturn(Instrument.newBuilder().setIsin("US21077C1071").setTicker("WISH").setInstrumentType("share").setName("Wish").build());
        when(instrumentsService.getInstrumentByFigiSync(eq("BBG004S681B4")))
            .thenReturn(Instrument.newBuilder().setIsin("RU0009046452").setTicker("NLMK").setInstrumentType("share").setName("NLMK").build());
        when(instrumentsService.getInstrumentByFigiSync(eq("US19260Q1076")))
            .thenReturn(Instrument.newBuilder().setIsin("US19260Q1076").setTicker("COIN").setInstrumentType("share").setName("Coinbase").build());
        when(instrumentsService.getInstrumentByFigiSync(eq("BBG0077VNXV6")))
            .thenReturn(Instrument.newBuilder().setIsin("US70450Y1038").setTicker("PYPL").setInstrumentType("share").setName("Paypal").build());
        when(instrumentsService.getInstrumentByFigiSync(eq("US0900401060")))
            .thenReturn(Instrument.newBuilder().setIsin("US0900401060").setTicker("US0900401060").setInstrumentType("share").setName("Bilibili Inc").build());
        when(instrumentsService.getInstrumentByFigiSync(eq("BBG000CH5208")))
            .thenReturn(Instrument.newBuilder().setIsin("US91324P1021").setTicker("UNH").setInstrumentType("share").setName("UnitedHealth").build());
        when(instrumentsService.getInstrumentByFigiSync(eq("BBG000BLKK03")))
            .thenReturn(Instrument.newBuilder().setIsin("US4448591028").setTicker("HUM").setInstrumentType("share").setName("Humana").build());
        when(instrumentsService.getInstrumentByFigiSync(eq("BBG001R3MNY9")))
            .thenReturn(Instrument.newBuilder().setIsin("US29355A1079").setTicker("ENPH").setInstrumentType("share").setName("Enphase Energy Inc").build());
        when(instrumentsService.getInstrumentByFigiSync(eq("BBG009S3NB30")))
            .thenReturn(Instrument.newBuilder().setIsin("BBG009S3NB30").setTicker("GOOG").setInstrumentType("share").setName("Google").build());


        var result = analyticService.analyzeAccount(ACCOUNT_ID, LocalDate.now().minusYears(6));

        var savedTickers = stockRepository.findAll();
        var savedOperations = operationRepository.findAll();
        assertAll(() -> {
            assertThat(result.getAnalyticData().size(), Matchers.greaterThan(10));
            assertThat(savedOperations.size(), Matchers.greaterThan(1000));
            result.getAnalyticData().forEach(analyticData -> {
                assertNotNull(analyticData.getTicker());
//                assertNotNull(analyticData.getFigi());
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