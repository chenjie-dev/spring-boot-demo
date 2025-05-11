package com.example.ratelimit.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {
    
    @Bean
    public RateLimiter rateLimiter() {
        // 每秒生成2个令牌
        return RateLimiter.create(2.0);
    }
} 