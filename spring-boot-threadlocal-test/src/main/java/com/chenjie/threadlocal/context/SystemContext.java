package com.chenjie.threadlocal.context;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 系统上下文信息
 * 用于存储当前请求的上下文信息，如操作IP、用户ID、请求ID等
 */
@Data
@Accessors(chain = true)
public class SystemContext {
    
    /**
     * 操作IP地址
     */
    private String clientIp;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 请求ID（用于链路追踪）
     */
    private String requestId;
    
    /**
     * 请求开始时间
     */
    private LocalDateTime requestTime;
    
    /**
     * 用户代理（浏览器信息）
     */
    private String userAgent;
    
    /**
     * 请求路径
     */
    private String requestPath;
    
    /**
     * 请求方法
     */
    private String requestMethod;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime = LocalDateTime.now();
    
    /**
     * 创建默认的系统上下文
     */
    public static SystemContext createDefault() {
        return new SystemContext()
                .setRequestTime(LocalDateTime.now())
                .setCreateTime(LocalDateTime.now());
    }
    
    /**
     * 获取上下文信息的字符串表示
     */
    @Override
    public String toString() {
        return String.format("SystemContext{clientIp='%s', userId='%s', username='%s', requestId='%s', requestPath='%s'}", 
                clientIp, userId, username, requestId, requestPath);
    }
} 