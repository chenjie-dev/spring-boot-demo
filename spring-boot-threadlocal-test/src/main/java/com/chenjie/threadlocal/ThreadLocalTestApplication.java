package com.chenjie.threadlocal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ThreadLocal 测试应用程序
 * 演示 TransmittableThreadLocal 的使用
 */
@SpringBootApplication
public class ThreadLocalTestApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ThreadLocalTestApplication.class, args);
        System.out.println("ThreadLocal Test Application started successfully!");
        System.out.println("Access the API at: http://localhost:8080/api/threadlocal/health");
    }
} 