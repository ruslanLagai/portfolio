package com.home.project.portfolio.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.home.project.portfolio.helpers.TestUtils;
import com.home.project.portfolio.model.entity.CompanyEntity;
import com.home.project.portfolio.model.portfolio.Sector;
import com.home.project.portfolio.repository.CompanyRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tinkoff.piapi.core.MarketDataService;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;

import static com.home.project.portfolio.utils.Constants.CURRENCY_FIGI_MAP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Test fpr {@link StockSectorService}
 *
 * @author rlagay
 */
@DisplayName("Test sector distribution")
@Testcontainers
@SpringBootTest
@ContextConfiguration(initializers = AbstractDbTest.Initializer.class)
class StockSectorServiceTest extends AbstractDbTest {

    private static final String POSITIONS_RESOURCE = "classpath:testData/positions-list.json";
    private static final String CURRENCY_PRICES_RESOURCE = "classpath:testData/get-currency-prices.json";

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private StockSectorService stockSectorService;

    @MockBean
    private MarketDataService marketDataService;

    @PostConstruct
    public void init() {
        companyRepository.save(CompanyEntity.builder()
                .ticker("TDOC")
                .sector("Healthcare")
                .name("Teladoc Health Inc")
                .industry("Health Information Services")
                .build());
    }

    @Test
    @DisplayName("Test sector data")
    void populateSectorData() {
        when(marketDataService.getLastPricesSync(eq(CURRENCY_FIGI_MAP.values())))
            .thenReturn(TestUtils.lastPrices(CURRENCY_PRICES_RESOURCE));

        var result = stockSectorService.getSectorData(TestUtils.getResource(POSITIONS_RESOURCE, new TypeReference<>() {}));
        var saved = companyRepository.findAll();
        assertAll(() -> {
            var tdoc = saved.stream().filter(companyEntity -> companyEntity.getTicker().equals("TDOC"))
                .findFirst().orElseThrow(AssertionError::new).getSector();
            assertEquals(90, saved.size());
            assertTrue(saved.stream().map(CompanyEntity::getTicker).collect(Collectors.toSet()).contains("TDOC"));
            assertEquals("Healthcare", tdoc);
        });
        assertAll(() -> {
            assertFalse(result.isEmpty());
            assertTrue(result.stream().anyMatch(sector -> sector.getSector().equals("Healthcare")));
            assertThat(result.stream()
                    .filter(sector -> sector.getSector().equals("Healthcare"))
                    .map(Sector::getSectorWeight)
                    .findFirst().orElse(0.0), Matchers.greaterThan(90.0));
            assertTrue(result.stream().anyMatch(sector -> sector.getSector().equals("Financial Services")));
            assertThat(result.stream()
                    .filter(sector -> sector.getSector().equals("Financial Services"))
                    .map(Sector::getSectorWeight)
                    .findFirst().orElse(0.0), Matchers.lessThan(10.0));
        });

        var sum = result.stream().map(Sector::getSectorWeight).mapToDouble(Double::doubleValue).sum();
        assertThat(sum, Matchers.greaterThan(99.0));
    }

}