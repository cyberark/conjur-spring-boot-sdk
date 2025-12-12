package com.cyberark.conjur.springboot.util;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;

/**
 * Configuration class to initialize and register Resilience4j CircuitBreakerRegistry and RetryRegistry.
 * Ensures singleton instances are set in ResilienceRegistryHolder for global access.
 */
public class ResilienceRegistryConfig {

    @PostConstruct
    public void init() {
        if (ResilienceRegistryHolder.getCircuitBreakerRegistry() == null) {
            ResilienceRegistryHolder.setCircuitBreakerRegistry(circuitBreakerRegistry());
        }

        if (ResilienceRegistryHolder.getRetryRegistry() == null) {
            ResilienceRegistryHolder.setRetryRegistry(retryRegistry());
        }
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }

    @Bean
    public RetryRegistry retryRegistry() {
        return RetryRegistry.ofDefaults();
    }

    public static void bootstrap() {
        if (ResilienceRegistryHolder.getCircuitBreakerRegistry() == null) {
            ResilienceRegistryHolder.setCircuitBreakerRegistry(CircuitBreakerRegistry.ofDefaults());
        }
        if (ResilienceRegistryHolder.getRetryRegistry() == null) {
            ResilienceRegistryHolder.setRetryRegistry(RetryRegistry.ofDefaults());
        }
    }
}
