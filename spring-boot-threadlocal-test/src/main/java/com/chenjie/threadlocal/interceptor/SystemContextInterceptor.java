package com.chenjie.threadlocal.interceptor;

import com.chenjie.threadlocal.context.SystemContext;
import com.chenjie.threadlocal.context.SystemContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * 系统上下文拦截器
 * 自动从HTTP请求中提取客户端IP、用户代理等信息并设置到系统上下文中
 */
@Slf4j
@Component
public class SystemContextInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // 创建系统上下文
            SystemContext context = SystemContext.createDefault();
            
            // 设置请求ID（如果请求头中没有，则生成新的）
            String requestId = request.getHeader("X-Request-ID");
            if (requestId == null || requestId.trim().isEmpty()) {
                requestId = UUID.randomUUID().toString().replace("-", "");
            }
            context.setRequestId(requestId);
            
            // 设置客户端IP
            String clientIp = getClientIp(request);
            context.setClientIp(clientIp);
            
            // 设置请求路径和方法
            context.setRequestPath(request.getRequestURI());
            context.setRequestMethod(request.getMethod());
            
            // 设置用户代理
            context.setUserAgent(request.getHeader("User-Agent"));
            
            // 设置用户信息（这里可以从JWT token或其他认证方式获取）
            // 示例：从请求头获取用户信息
            String userId = request.getHeader("X-User-ID");
            String username = request.getHeader("X-Username");
            if (userId != null) {
                context.setUserId(userId);
            }
            if (username != null) {
                context.setUsername(username);
            }
            
            // 将上下文设置到 TransmittableThreadLocal 中
            SystemContextHolder.setContext(context);
            
            log.info("System context initialized - RequestId: {}, ClientIP: {}, Path: {}, Method: {}", 
                    requestId, clientIp, request.getRequestURI(), request.getMethod());
            
        } catch (Exception e) {
            log.error("Error initializing system context", e);
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            // 请求完成后清理上下文
            SystemContextHolder.clearContext();
            log.debug("System context cleared after request completion");
        } catch (Exception e) {
            log.error("Error clearing system context", e);
        }
    }
    
    /**
     * 获取客户端真实IP地址
     * 考虑了代理服务器的情况
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 如果是多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip != null ? ip : "unknown";
    }
} 