package com.homektv.ai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AiAsyncConfig {
    @Bean(name = "aiBulkExecutor")
    public ThreadPoolTaskExecutor aiBulkExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("ai-bulk-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "aiReasoningExecutor")
    public ThreadPoolTaskExecutor aiReasoningExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("ai-reasoning-");
        executor.initialize();
        return executor;
    }
}
