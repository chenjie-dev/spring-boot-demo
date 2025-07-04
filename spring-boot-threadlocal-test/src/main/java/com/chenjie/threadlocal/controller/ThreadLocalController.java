package com.chenjie.threadlocal.controller;

import com.chenjie.threadlocal.context.SystemContextHolder;
import com.chenjie.threadlocal.service.AsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * ThreadLocal 演示控制器
 * 提供API接口来演示 TransmittableThreadLocal 的使用
 */
@Slf4j
@RestController
@RequestMapping("/api/threadlocal")
@RequiredArgsConstructor
public class ThreadLocalController {
    
    private final AsyncService asyncService;
    
    /**
     * 获取当前系统上下文信息
     */
    @GetMapping("/context")
    public Map<String, Object> getCurrentContext() {
        log.info("Getting current system context");
        
        Map<String, Object> result = new HashMap<>();
        result.put("clientIp", SystemContextHolder.getClientIp());
        result.put("userId", SystemContextHolder.getUserId());
        result.put("username", SystemContextHolder.getUsername());
        result.put("requestId", SystemContextHolder.getRequestId());
        result.put("requestPath", SystemContextHolder.getRequestPath());
        result.put("requestMethod", SystemContextHolder.getRequestMethod());
        result.put("userAgent", SystemContextHolder.getUserAgent());
        result.put("threadName", Thread.currentThread().getName());
        
        log.info("Current context: {}", result);
        return result;
    }
    
    /**
     * 设置用户信息
     */
    @PostMapping("/user")
    public Map<String, Object> setUserInfo(@RequestParam String userId, @RequestParam String username) {
        log.info("Setting user info - UserID: {}, Username: {}", userId, username);
        
        SystemContextHolder.setUserId(userId);
        SystemContextHolder.setUsername(username);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "User info set successfully");
        result.put("userId", SystemContextHolder.getUserId());
        result.put("username", SystemContextHolder.getUsername());
        result.put("requestId", SystemContextHolder.getRequestId());
        
        return result;
    }
    
    /**
     * 执行异步任务
     */
    @PostMapping("/async-task")
    public CompletableFuture<Map<String, Object>> executeAsyncTask(@RequestParam String taskName) {
        log.info("Executing async task: {}", taskName);
        
        return asyncService.processAsyncTask(taskName)
                .thenApply(result -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("taskName", taskName);
                    response.put("result", result);
                    response.put("mainThreadContext", Map.of(
                            "clientIp", SystemContextHolder.getClientIp(),
                            "requestId", SystemContextHolder.getRequestId(),
                            "threadName", Thread.currentThread().getName()
                    ));
                    return response;
                });
    }
    
    /**
     * 执行批量异步任务
     */
    @PostMapping("/batch-task")
    public CompletableFuture<Map<String, Object>> executeBatchTask(
            @RequestParam String batchName, 
            @RequestParam(defaultValue = "5") int count) {
        log.info("Executing batch task: {} with count: {}", batchName, count);
        
        return asyncService.processBatchTask(batchName, count)
                .thenApply(result -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("batchName", batchName);
                    response.put("count", count);
                    response.put("result", result);
                    response.put("mainThreadContext", Map.of(
                            "clientIp", SystemContextHolder.getClientIp(),
                            "requestId", SystemContextHolder.getRequestId(),
                            "threadName", Thread.currentThread().getName()
                    ));
                    return response;
                });
    }
    
    /**
     * 演示多个异步任务并发执行
     */
    @PostMapping("/concurrent-tasks")
    public CompletableFuture<Map<String, Object>> executeConcurrentTasks() {
        log.info("Executing concurrent tasks");
        
        CompletableFuture<String> task1 = asyncService.processAsyncTask("Task-1");
        CompletableFuture<String> task2 = asyncService.processAsyncTask("Task-2");
        CompletableFuture<String> task3 = asyncService.processAsyncTask("Task-3");
        
        return CompletableFuture.allOf(task1, task2, task3)
                .thenApply(v -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("task1", task1.join());
                    response.put("task2", task2.join());
                    response.put("task3", task3.join());
                    response.put("mainThreadContext", Map.of(
                            "clientIp", SystemContextHolder.getClientIp(),
                            "requestId", SystemContextHolder.getRequestId(),
                            "threadName", Thread.currentThread().getName()
                    ));
                    return response;
                });
    }
    
    /**
     * 打印当前上下文信息到日志
     */
    @PostMapping("/print-context")
    public Map<String, Object> printContext() {
        log.info("Printing current context");
        asyncService.printCurrentContext();
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Context printed to logs");
        result.put("context", Map.of(
                "clientIp", SystemContextHolder.getClientIp(),
                "userId", SystemContextHolder.getUserId(),
                "username", SystemContextHolder.getUsername(),
                "requestId", SystemContextHolder.getRequestId(),
                "threadName", Thread.currentThread().getName()
        ));
        
        return result;
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("message", "ThreadLocal demo service is running");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
} 