package com.home.project.portfolio.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tinkoff.api")
public record TinkoffProperties (String url, String token) {
}
