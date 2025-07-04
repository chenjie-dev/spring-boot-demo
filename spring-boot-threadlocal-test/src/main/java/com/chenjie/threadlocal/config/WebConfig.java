package com.chenjie.threadlocal.config;

import com.chenjie.threadlocal.interceptor.SystemContextInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 注册系统上下文拦截器
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final SystemContextInterceptor systemContextInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册系统上下文拦截器，应用到所有请求
        registry.addInterceptor(systemContextInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error"); // 排除错误页面
    }
} 