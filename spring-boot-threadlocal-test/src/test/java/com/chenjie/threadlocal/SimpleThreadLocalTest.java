package com.chenjie.threadlocal;

import com.chenjie.threadlocal.context.SystemContext;
import com.chenjie.threadlocal.context.SystemContextHolder;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 简单的 ThreadLocal 测试类
 * 不依赖 Spring 上下文，直接测试 TransmittableThreadLocal 功能
 */
class SimpleThreadLocalTest {
    
    @Test
    void testSystemContextHolder() {
        // 测试系统上下文持有者
        SystemContext context = SystemContext.createDefault()
                .setClientIp("192.168.1.100")
                .setUserId("user123")
                .setUsername("testuser");
        
        SystemContextHolder.setContext(context);
        
        assertEquals("192.168.1.100", SystemContextHolder.getClientIp());
        assertEquals("user123", SystemContextHolder.getUserId());
        assertEquals("testuser", SystemContextHolder.getUsername());
        
        // 清理上下文
        SystemContextHolder.clearContext();
    }
    
    @Test
    void testTransmittableThreadLocal() throws Exception {
        // 测试 TransmittableThreadLocal 在线程间的传递
        SystemContext context = SystemContext.createDefault()
                .setClientIp("192.168.1.200")
                .setUserId("user456")
                .setUsername("testuser2");
        
        SystemContextHolder.setContext(context);
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        try {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                // 在新线程中获取上下文信息
                String clientIp = SystemContextHolder.getClientIp();
                String userId = SystemContextHolder.getUserId();
                String username = SystemContextHolder.getUsername();
                
                return String.format("Thread: %s, IP: %s, UserID: %s, Username: %s",
                        Thread.currentThread().getName(), clientIp, userId, username);
            }, executor);
            
            String result = future.get();
            System.out.println("Async result: " + result);
            
            // 验证结果包含正确的上下文信息
            assertTrue(result.contains("192.168.1.200"));
            assertTrue(result.contains("user456"));
            assertTrue(result.contains("testuser2"));
            
        } finally {
            executor.shutdown();
            SystemContextHolder.clearContext();
        }
    }
    
    @Test
    void testRequestIdGeneration() {
        // 测试请求ID生成
        SystemContext context = SystemContext.createDefault();
        SystemContextHolder.setContext(context);
        
        String requestId1 = SystemContextHolder.getRequestId();
        String requestId2 = SystemContextHolder.getRequestId();
        
        // 同一个上下文中，请求ID应该相同
        assertEquals(requestId1, requestId2);
        
        // 请求ID应该不为空且长度合理
        assertNotNull(requestId1);
        assertTrue(requestId1.length() > 0);
        
        SystemContextHolder.clearContext();
    }
    
    @Test
    void testContextModification() {
        // 测试上下文修改
        SystemContext context = SystemContext.createDefault()
                .setClientIp("192.168.1.100")
                .setUserId("user123");
        
        SystemContextHolder.setContext(context);
        
        // 修改上下文
        SystemContextHolder.setUsername("newuser");
        SystemContextHolder.setClientIp("192.168.1.200");
        
        assertEquals("192.168.1.200", SystemContextHolder.getClientIp());
        assertEquals("user123", SystemContextHolder.getUserId());
        assertEquals("newuser", SystemContextHolder.getUsername());
        
        SystemContextHolder.clearContext();
    }
} 