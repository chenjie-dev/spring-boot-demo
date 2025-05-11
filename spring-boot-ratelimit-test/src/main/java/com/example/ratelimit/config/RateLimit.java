package com.example.ratelimit.config;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    /**
     * 限流时间窗口，单位秒
     */
    int time() default 1;

    /**
     * 限流次数
     */
    int count() default 1;
} 