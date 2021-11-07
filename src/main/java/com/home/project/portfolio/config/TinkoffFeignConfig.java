package com.home.project.portfolio.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Feign client config
 */
@Configuration
public class TinkoffFeignConfig {
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    Retryer retryer() {
        return new Retryer.Default(1000, SECONDS.toMillis(1), 5);
    }

    @Bean
    ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    @Bean
    RequestInterceptor clientInterceptor() {
        return new FeignClientInterceptor();
    }

}
