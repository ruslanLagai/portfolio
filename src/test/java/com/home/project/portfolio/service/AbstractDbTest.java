package com.home.project.portfolio.service;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

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
}
