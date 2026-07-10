package com.bidhutkarki.tictactoe.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.service")
public record AuthServiceProperties(String baseUrl) {
}
