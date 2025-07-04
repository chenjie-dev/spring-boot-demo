package com.chenjie.threadlocal.service;

import com.chenjie.threadlocal.context.SystemContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 异步服务类
 * 演示在线程池中使用 TransmittableThreadLocal 传递上下文信息
 */
@Slf4j
@Service
public class AsyncService {
    
    /**
     * 异步处理任务
     * 使用 @Async 注解，会在不同的线程中执行
     * TransmittableThreadLocal 会自动将上下文信息传递到新线程
     */
    @Async
    public CompletableFuture<String> processAsyncTask(String taskName) {
        try {
            // 获取当前线程的系统上下文信息
            String clientIp = SystemContextHolder.getClientIp();
            String userId = SystemContextHolder.getUserId();
            String requestId = SystemContextHolder.getRequestId();
            String username = SystemContextHolder.getUsername();
            
            log.info("Async task '{}' started - Thread: {}, ClientIP: {}, UserID: {}, RequestID: {}, Username: {}", 
                    taskName, Thread.currentThread().getName(), clientIp, userId, requestId, username);
            
            // 模拟异步处理
            TimeUnit.MILLISECONDS.sleep(100);
            
            // 在异步任务中也可以修改上下文信息
            SystemContextHolder.setUsername(username + "_async");
            
            String result = String.format("Task '%s' completed successfully. Context: ClientIP=%s, UserID=%s, RequestID=%s", 
                    taskName, clientIp, userId, requestId);
            
            log.info("Async task '{}' completed - Result: {}", taskName, result);
            
            return CompletableFuture.completedFuture(result);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Async task '{}' was interrupted", taskName, e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("Async task '{}' failed", taskName, e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * 批量异步处理任务
     */
    @Async
    public CompletableFuture<String> processBatchTask(String batchName, int count) {
        try {
            String clientIp = SystemContextHolder.getClientIp();
            String requestId = SystemContextHolder.getRequestId();
            
            log.info("Batch task '{}' started - Thread: {}, ClientIP: {}, RequestID: {}, Count: {}", 
                    batchName, Thread.currentThread().getName(), clientIp, requestId, count);
            
            // 模拟批量处理
            for (int i = 0; i < count; i++) {
                TimeUnit.MILLISECONDS.sleep(50);
                log.debug("Processing batch item {}/{} in thread {}", i + 1, count, Thread.currentThread().getName());
            }
            
            String result = String.format("Batch task '%s' completed. Processed %d items. Context: ClientIP=%s, RequestID=%s", 
                    batchName, count, clientIp, requestId);
            
            log.info("Batch task '{}' completed - Result: {}", batchName, result);
            
            return CompletableFuture.completedFuture(result);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Batch task '{}' was interrupted", batchName, e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("Batch task '{}' failed", batchName, e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * 打印当前上下文信息
     */
    public void printCurrentContext() {
        SystemContextHolder.printContext();
    }
} 