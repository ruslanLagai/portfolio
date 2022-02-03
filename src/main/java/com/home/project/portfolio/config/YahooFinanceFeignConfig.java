package com.home.project.portfolio.config;

import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Feign client config
 */
@Configuration
public class YahooFinanceFeignConfig {
    @Bean
    Logger.Level alphaVantageLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    Retryer alphaVantageRetryer() {
        return new Retryer.Default(2000, SECONDS.toMillis(25), 5);
    }

    @Bean
    ErrorDecoder alphaVantageErrorDecoder() {
        return new FeignErrorDecoder();
    }
}
