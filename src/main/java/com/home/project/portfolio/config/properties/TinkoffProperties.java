package com.home.project.portfolio.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("tinkoff")
@Component
@Data
public class TinkoffProperties {
    public String url;
    public String token;
}
