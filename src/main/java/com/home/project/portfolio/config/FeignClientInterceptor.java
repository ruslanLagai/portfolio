package com.home.project.portfolio.config;

import com.home.project.portfolio.security.CurrentUser;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Interceptor to add authorization header
 */
public class FeignClientInterceptor implements RequestInterceptor {

    @Value("${tinkoff.api.token}")
    private String token;

    private CurrentUser currentUser;

    @Autowired
    public void setCurrentUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("Authorization",
                String.format("Bearer %s", currentUser.getTinkoffToken().orElse(null)));
    }
}
