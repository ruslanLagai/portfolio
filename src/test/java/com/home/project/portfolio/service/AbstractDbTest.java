package com.home.project.portfolio.service;

import com.home.project.portfolio.client.YahooFinanceClient;
import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.helpers.YamlPropertySourceFactory;
import com.home.project.portfolio.repository.CompanyRepository;
import com.home.project.portfolio.repository.OperationRepository;
import com.home.project.portfolio.repository.StockRepository;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import static com.home.project.portfolio.utils.Profiles.CUSTOM_DB_TEST_PROFILE;

public abstract class AbstractDbTest {

    @Container
    protected static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8");

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
                "spring.flyway.schemas=" + mySQLContainer.getDatabaseName(),
                "spring.flyway.user=" + mySQLContainer.getUsername());
        }
    }

    @Profile(CUSTOM_DB_TEST_PROFILE)
    @TestConfiguration
    @Import({FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
    @EnableJpaRepositories("com.home.project.portfolio.repository")
    @EnableFeignClients(clients = {TinkoffClient.class, YahooFinanceClient.class})
    @PropertySource(value = "classpath:application-test.yml", factory = YamlPropertySourceFactory.class)
    static class Config {
        @Bean
        public StockHelperService stockHelperService(TinkoffClient tinkoffClient, StockRepository stockRepository) {
            return new StockHelperService(tinkoffClient, stockRepository);
        }

        @Bean
        public OperationsService operationsService(TinkoffClient tinkoffClient, OperationRepository operationRepository,
                                                   StockHelperService stockHelperService) {
            return new OperationsService(tinkoffClient, operationRepository, stockHelperService);
        }

        @Bean
        public StockSectorService stockSectorService(YahooFinanceClient alphaVantageClient,
                                                     CompanyRepository companyRepository,
                                                     CurrencyService currencyService) {
            return new StockSectorService(alphaVantageClient, companyRepository, currencyService);
        }

//        @Bean
//        public CurrencyService currencyService(TinkoffClient tinkoffClient) {
//             return new CurrencyService(tinkoffClient);
//        }

        @Bean
        public DataSource dataSource() {
            return DataSourceBuilder.create()
                    .url(mySQLContainer.getJdbcUrl())
                    .password(mySQLContainer.getPassword())
                    .username(mySQLContainer.getUsername())
                    .driverClassName(mySQLContainer.getDriverClassName())
                    .build();
        }

        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            vendorAdapter.setGenerateDdl(true);
            vendorAdapter.setDatabase(Database.MYSQL);

            LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
            factory.setJpaVendorAdapter(vendorAdapter);
            factory.setJpaDialect(new HibernateJpaDialect());
            factory.setPackagesToScan("com.home.project.portfolio.model.entity");
            factory.setDataSource(dataSource);
            return factory;
        }

        @Bean
        public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {

            JpaTransactionManager txManager = new JpaTransactionManager();
            txManager.setEntityManagerFactory(entityManagerFactory);
            return txManager;
        }
    }
}
