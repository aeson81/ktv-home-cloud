package com.homektv.musicsource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class MetadataScrapeAsyncConfig {
    @Bean("metadataScrapeExecutor")
    public ThreadPoolTaskExecutor metadataScrapeExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("metadata-scrape-");
        executor.initialize();
        return executor;
    }
}
