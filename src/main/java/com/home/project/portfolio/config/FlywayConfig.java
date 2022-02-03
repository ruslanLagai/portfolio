package com.home.project.portfolio.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * @author rlagay
 */
@Component
public class FlywayConfig {

    private final ApplicationContext applicationContext;
    private final DataSource dataSource;

    @Value("${spring.flyway.schemas}")
    private String schema;

    @Value("${spring.flyway.locations}")
    private String locations;

    public FlywayConfig(ApplicationContext applicationContext,
                        DataSource dataSource) {
        this.applicationContext = applicationContext;
        this.dataSource = dataSource;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().equals(applicationContext)) {
            Flyway.configure().dataSource(dataSource)
                    .baselineOnMigrate(true)
                    .schemas(schema)
                    .locations(locations)
                    .load().migrate();
        }
    }
}
