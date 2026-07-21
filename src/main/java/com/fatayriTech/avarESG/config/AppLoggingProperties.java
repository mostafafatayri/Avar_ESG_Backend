package com.fatayriTech.avarESG.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.logging")
public class AppLoggingProperties {

    private boolean verbose;
}