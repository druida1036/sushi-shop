package com.livebarn.sushishop.config;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ExecutorConfig {

    private static final String CHEF_NAME_PREFIX = "Chef-";

    @Bean
    public Executor chefManagerExecutor(
        @Value("${sushi.shop.chef.available-number:3}") int chefCount,
        @Value("${sushi.shop.chef.order-queue-capacity:1000}") int queueCapacity
    ) {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(chefCount);
        executor.setMaxPoolSize(chefCount);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(CHEF_NAME_PREFIX);
        executor.initialize();
        return executor;
    }
}
