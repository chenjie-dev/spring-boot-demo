package com.chenjie.threadlocal.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * 系统上下文持有者
 * 使用 TransmittableThreadLocal 在线程间传递系统上下文信息
 */
@Slf4j
public class SystemContextHolder {
    
    /**
     * 使用 TransmittableThreadLocal 存储系统上下文
     * TransmittableThreadLocal 可以在线程池场景下正确传递上下文信息
     */
    private static final TransmittableThreadLocal<SystemContext> CONTEXT_HOLDER = new TransmittableThreadLocal<>();
    
    /**
     * 设置系统上下文
     */
    public static void setContext(SystemContext context) {
        if (context == null) {
            log.warn("Attempting to set null context, ignoring");
            return;
        }
        CONTEXT_HOLDER.set(context);
        log.debug("Set system context: {}", context);
    }
    
    /**
     * 获取系统上下文
     */
    public static SystemContext getContext() {
        SystemContext context = CONTEXT_HOLDER.get();
        if (context == null) {
            log.warn("System context is null, creating default context");
            context = SystemContext.createDefault();
            setContext(context);
        }
        return context;
    }
    
    /**
     * 清除系统上下文
     */
    public static void clearContext() {
        SystemContext context = CONTEXT_HOLDER.get();
        if (context != null) {
            log.debug("Clearing system context: {}", context);
        }
        CONTEXT_HOLDER.remove();
    }
    
    /**
     * 获取客户端IP
     */
    public static String getClientIp() {
        return getContext().getClientIp();
    }
    
    /**
     * 设置客户端IP
     */
    public static void setClientIp(String clientIp) {
        getContext().setClientIp(clientIp);
    }
    
    /**
     * 获取用户ID
     */
    public static String getUserId() {
        return getContext().getUserId();
    }
    
    /**
     * 设置用户ID
     */
    public static void setUserId(String userId) {
        getContext().setUserId(userId);
    }
    
    /**
     * 获取用户名
     */
    public static String getUsername() {
        return getContext().getUsername();
    }
    
    /**
     * 设置用户名
     */
    public static void setUsername(String username) {
        getContext().setUsername(username);
    }
    
    /**
     * 获取请求ID
     */
    public static String getRequestId() {
        SystemContext context = getContext();
        if (context.getRequestId() == null) {
            // 如果请求ID为空，生成一个新的
            String requestId = UUID.randomUUID().toString().replace("-", "");
            context.setRequestId(requestId);
            log.debug("Generated new request ID: {}", requestId);
        }
        return context.getRequestId();
    }
    
    /**
     * 设置请求ID
     */
    public static void setRequestId(String requestId) {
        getContext().setRequestId(requestId);
    }
    
    /**
     * 获取请求路径
     */
    public static String getRequestPath() {
        return getContext().getRequestPath();
    }
    
    /**
     * 设置请求路径
     */
    public static void setRequestPath(String requestPath) {
        getContext().setRequestPath(requestPath);
    }
    
    /**
     * 获取请求方法
     */
    public static String getRequestMethod() {
        return getContext().getRequestMethod();
    }
    
    /**
     * 设置请求方法
     */
    public static void setRequestMethod(String requestMethod) {
        getContext().setRequestMethod(requestMethod);
    }
    
    /**
     * 获取用户代理
     */
    public static String getUserAgent() {
        return getContext().getUserAgent();
    }
    
    /**
     * 设置用户代理
     */
    public static void setUserAgent(String userAgent) {
        getContext().setUserAgent(userAgent);
    }
    
    /**
     * 打印当前上下文信息
     */
    public static void printContext() {
        SystemContext context = getContext();
        log.info("Current System Context: {}", context);
    }
} 