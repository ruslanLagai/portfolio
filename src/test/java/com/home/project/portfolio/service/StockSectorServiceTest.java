package com.home.project.portfolio.service;

import com.home.project.portfolio.helpers.TestUtils;
import com.home.project.portfolio.model.entity.CompanyEntity;
import com.home.project.portfolio.model.portfolio.Portfolio;
import com.home.project.portfolio.model.portfolio.Sector;
import com.home.project.portfolio.repository.CompanyRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;

import static com.home.project.portfolio.utils.Profiles.TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test fpr {@link StockSectorService}
 *
 * @author rlagay
 */
@ActiveProfiles(TEST_PROFILE)
@DisplayName("Test sector distribution")
@Testcontainers
@SpringBootTest
@ContextConfiguration(initializers = StockSectorServiceTest.Initializer.class)
class StockSectorServiceTest extends AbstractDbTest {

    private final Portfolio portfolio = TestUtils.readPositions();

    static {
        mySQLContainer.start();
    }

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    StockSectorService stockSectorService;

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
    void populateSectorData() {
        var result = stockSectorService.getSectorData(portfolio.getPayload().getPositions());
        var saved = companyRepository.findAll();
        assertAll(() -> {
            assertEquals(99, saved.size());
            assertTrue(saved.stream().map(CompanyEntity::getTicker).collect(Collectors.toSet()).contains("TDOC"));
            assertTrue(saved.stream().map(CompanyEntity::getSector).collect(Collectors.toSet()).contains("Healthcare"));
        });
        assertAll(() -> {
            assertFalse(result.isEmpty());
            assertTrue(result.stream().anyMatch(sector -> sector.getSector().equals("Healthcare")));
            assertThat(result.stream()
                    .filter(sector -> sector.getSector().equals("Healthcare"))
                    .map(Sector::getSectorWeight)
                    .findFirst().orElse(0.0), Matchers.greaterThan(5.0));
            assertTrue(result.stream().anyMatch(sector -> sector.getSector().equals("Financial Services")));
            assertThat(result.stream()
                    .filter(sector -> sector.getSector().equals("Financial Services"))
                    .map(Sector::getSectorWeight)
                    .findFirst().orElse(0.0), Matchers.greaterThan(10.0));
        });

        var sum = result.stream().map(Sector::getSectorWeight).mapToDouble(Double::doubleValue).sum();
        assertThat(sum, Matchers.greaterThan(99.0));
    }


    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    configurableApplicationContext, "spring.datasource.url=" + mySQLContainer.getJdbcUrl(),
                    "spring.datasource.password=" + mySQLContainer.getPassword(),
                    "spring.datasource.username=" + mySQLContainer.getUsername(),
                    "spring.datasource.driver-class-name=" + mySQLContainer.getDriverClassName(),
                    "spring.flyway.url=" + mySQLContainer.getJdbcUrl(),
                    "spring.flyway.password=" + mySQLContainer.getPassword(),
                    "spring.flyway.user=" + mySQLContainer.getUsername());
        }
    }
}