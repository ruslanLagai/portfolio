package com.home.project.portfolio.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;

/**
 * Interceptor to add authorization header
 */
public class FeignClientInterceptor implements RequestInterceptor {

    @Value("${tinkoff.api.token}")
    private String token;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("Authorization",
                String.format("Bearer %s", token));
    }
}
