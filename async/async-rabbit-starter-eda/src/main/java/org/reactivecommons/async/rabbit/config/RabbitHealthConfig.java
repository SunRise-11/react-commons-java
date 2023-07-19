package org.reactivecommons.async.rabbit.config;

import org.reactivecommons.async.rabbit.health.RabbitReactiveHealthIndicator;
import org.springframework.boot.actuate.health.AbstractReactiveHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.reactivecommons.async.api.HandlerRegistry.DEFAULT_DOMAIN;

@Configuration
@ConditionalOnClass(AbstractReactiveHealthIndicator.class)
public class RabbitHealthConfig {

    @Bean
    public RabbitReactiveHealthIndicator rabbitHealthIndicator(ConnectionManager manager) {
        return new RabbitReactiveHealthIndicator(manager.getProvider(DEFAULT_DOMAIN)); // TODO: Check every domain connection
    }
}
