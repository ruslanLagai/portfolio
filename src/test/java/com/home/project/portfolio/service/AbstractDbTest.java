package com.home.project.portfolio.service;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.helpers.YamlPropertySourceFactory;
import com.home.project.portfolio.repository.OperationRepository;
import com.home.project.portfolio.repository.StockRepository;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

public abstract class AbstractDbTest {

    @Container
    protected static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8");

    @Configuration
    @Import({FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
    @EnableJpaRepositories("com.home.project.portfolio.repository")
    @EnableFeignClients(clients = TinkoffClient.class)
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
