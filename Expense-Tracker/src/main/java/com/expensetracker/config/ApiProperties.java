package com.expensetracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "api.security")
public class ApiProperties {

    private String ignored;
}
