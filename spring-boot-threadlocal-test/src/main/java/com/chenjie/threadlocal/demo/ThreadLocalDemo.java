package com.chenjie.threadlocal.demo;

import com.chenjie.threadlocal.context.SystemContext;
import com.chenjie.threadlocal.context.SystemContextHolder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TransmittableThreadLocal 演示程序
 * 展示如何在线程间传递上下文信息
 */
public class ThreadLocalDemo {
    
    public static void main(String[] args) {
        System.out.println("=== TransmittableThreadLocal 演示 ===");
        
        // 演示1: 基本使用
        basicDemo();
        
        // 演示2: 线程池中的上下文传递
        threadPoolDemo();
        
        // 演示3: 异步任务中的上下文传递
        asyncTaskDemo();
        
        System.out.println("=== 演示完成 ===");
    }
    
    /**
     * 基本使用演示
     */
    private static void basicDemo() {
        System.out.println("\n--- 基本使用演示 ---");
        
        // 创建系统上下文
        SystemContext context = SystemContext.createDefault()
                .setClientIp("192.168.1.100")
                .setUserId("user123")
                .setUsername("张三")
                .setRequestPath("/api/test");
        
        // 设置上下文
        SystemContextHolder.setContext(context);
        
        // 获取上下文信息
        System.out.println("当前线程: " + Thread.currentThread().getName());
        System.out.println("客户端IP: " + SystemContextHolder.getClientIp());
        System.out.println("用户ID: " + SystemContextHolder.getUserId());
        System.out.println("用户名: " + SystemContextHolder.getUsername());
        System.out.println("请求路径: " + SystemContextHolder.getRequestPath());
        System.out.println("请求ID: " + SystemContextHolder.getRequestId());
        
        // 清理上下文
        SystemContextHolder.clearContext();
    }
    
    /**
     * 线程池中的上下文传递演示
     */
    private static void threadPoolDemo() {
        System.out.println("\n--- 线程池中的上下文传递演示 ---");
        
        // 创建系统上下文
        SystemContext context = SystemContext.createDefault()
                .setClientIp("192.168.1.200")
                .setUserId("user456")
                .setUsername("李四");
        
        SystemContextHolder.setContext(context);
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        try {
            // 提交任务到线程池
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                System.out.println("线程池任务执行 - 线程: " + Thread.currentThread().getName());
                System.out.println("  客户端IP: " + SystemContextHolder.getClientIp());
                System.out.println("  用户ID: " + SystemContextHolder.getUserId());
                System.out.println("  用户名: " + SystemContextHolder.getUsername());
                
                // 在子线程中修改上下文
                SystemContextHolder.setUsername(SystemContextHolder.getUsername() + "_子线程");
                
                return "任务完成";
            }, executor);
            
            // 等待任务完成
            String result = future.get();
            System.out.println("主线程结果: " + result);
            
            // 主线程的上下文应该没有变化
            System.out.println("主线程上下文 - 用户名: " + SystemContextHolder.getUsername());
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
            SystemContextHolder.clearContext();
        }
    }
    
    /**
     * 异步任务中的上下文传递演示
     */
    private static void asyncTaskDemo() {
        System.out.println("\n--- 异步任务中的上下文传递演示 ---");
        
        // 创建系统上下文
        SystemContext context = SystemContext.createDefault()
                .setClientIp("192.168.1.300")
                .setUserId("user789")
                .setUsername("王五");
        
        SystemContextHolder.setContext(context);
        
        // 创建多个异步任务
        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
            return processTask("任务1");
        });
        
        CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> {
            return processTask("任务2");
        });
        
        CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> {
            return processTask("任务3");
        });
        
        // 等待所有任务完成
        CompletableFuture.allOf(task1, task2, task3).join();
        
        try {
            System.out.println("任务1结果: " + task1.get());
            System.out.println("任务2结果: " + task2.get());
            System.out.println("任务3结果: " + task3.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SystemContextHolder.clearContext();
    }
    
    /**
     * 处理任务的辅助方法
     */
    private static String processTask(String taskName) {
        System.out.println(taskName + " 开始执行 - 线程: " + Thread.currentThread().getName());
        System.out.println("  上下文信息:");
        System.out.println("    客户端IP: " + SystemContextHolder.getClientIp());
        System.out.println("    用户ID: " + SystemContextHolder.getUserId());
        System.out.println("    用户名: " + SystemContextHolder.getUsername());
        System.out.println("    请求ID: " + SystemContextHolder.getRequestId());
        
        // 模拟处理时间
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println(taskName + " 执行完成");
        return taskName + " 完成，处理用户: " + SystemContextHolder.getUsername();
    }
} 