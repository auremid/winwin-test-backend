package com.winwin.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @NotNull Jwt jwt,
        @NotNull DataApi dataApi
) {
    public record Jwt(
            @NotBlank String secret,
            @NotNull Duration expiration
    ) {
    }

    public record DataApi(
            @NotBlank String url,
            @NotBlank String internalToken,
            @NotNull Duration connectTimeout,
            @NotNull Duration readTimeout
    ) {
    }
}
