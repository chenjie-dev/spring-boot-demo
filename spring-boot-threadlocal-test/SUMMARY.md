# TransmittableThreadLocal 模块总结

## 模块概述

这个模块演示了如何使用 `TransmittableThreadLocal` 来完善记录系统上下文，特别是在线程池场景下正确传递上下文信息。

## 核心功能

### 1. 系统上下文管理
- **SystemContext**: 存储请求上下文信息（IP、用户ID、请求ID等）
- **SystemContextHolder**: 使用 TransmittableThreadLocal 管理上下文

### 2. 自动上下文提取
- **SystemContextInterceptor**: 从HTTP请求中自动提取客户端信息
- 支持多种代理头（X-Forwarded-For、Proxy-Client-IP等）
- 自动生成请求ID用于链路追踪

### 3. 线程池上下文传递
- 在异步任务中正确传递上下文信息
- 支持 @Async 注解的异步方法
- 演示线程池中的上下文隔离

## 主要特性

### TransmittableThreadLocal vs ThreadLocal

| 特性 | ThreadLocal | TransmittableThreadLocal |
|------|-------------|-------------------------|
| 线程隔离 | ✅ | ✅ |
| 线程池传递 | ❌ | ✅ |
| 异步任务支持 | ❌ | ✅ |
| 内存泄漏风险 | 高 | 低 |

### 上下文信息

- **客户端IP**: 自动从请求头提取，支持代理场景
- **用户信息**: 用户ID、用户名
- **请求信息**: 请求ID、路径、方法
- **时间信息**: 请求时间、创建时间
- **用户代理**: 浏览器信息

## 使用场景

### 1. 链路追踪
```java
// 在异步任务中获取请求ID
String requestId = SystemContextHolder.getRequestId();
log.info("处理请求: {}", requestId);
```

### 2. 用户行为记录
```java
// 记录用户操作
String userId = SystemContextHolder.getUserId();
String clientIp = SystemContextHolder.getClientIp();
auditService.recordUserAction(userId, clientIp, action);
```

### 3. 日志增强
```java
// 在日志中包含上下文信息
log.info("用户 {} 从 {} 访问了 {}", 
    SystemContextHolder.getUsername(),
    SystemContextHolder.getClientIp(),
    SystemContextHolder.getRequestPath());
```

## 技术实现

### 1. 依赖管理
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>transmittable-thread-local</artifactId>
    <version>2.15.2</version>
</dependency>
```

### 2. 核心类结构
```
com.chenjie.threadlocal
├── context/
│   ├── SystemContext.java          # 上下文数据模型
│   └── SystemContextHolder.java    # TransmittableThreadLocal 管理器
├── interceptor/
│   └── SystemContextInterceptor.java # HTTP请求拦截器
├── config/
│   ├── WebConfig.java              # Web配置
│   └── AsyncConfig.java            # 异步配置
├── service/
│   └── AsyncService.java           # 异步服务示例
├── controller/
│   └── ThreadLocalController.java  # REST API控制器
└── demo/
    └── ThreadLocalDemo.java        # 演示程序
```

### 3. 配置要点

#### application.yml
```yaml
spring:
  task:
    execution:
      pool:
        core-size: 5
        max-size: 10
        queue-capacity: 100
      thread-name-prefix: "AsyncThread-"
```

#### 拦截器配置
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(systemContextInterceptor)
                .addPathPatterns("/**");
    }
}
```

## 最佳实践

### 1. 上下文清理
```java
try {
    // 执行业务逻辑
    businessService.process();
} finally {
    // 确保清理上下文
    SystemContextHolder.clearContext();
}
```

### 2. 异常处理
```java
@Async
public CompletableFuture<String> processAsyncTask(String taskName) {
    try {
        // 异步处理逻辑
        return CompletableFuture.completedFuture(result);
    } catch (Exception e) {
        log.error("异步任务失败: {}", taskName, e);
        return CompletableFuture.failedFuture(e);
    }
}
```

### 3. 性能考虑
- 避免在上下文中存储大量数据
- 及时清理不需要的上下文信息
- 合理配置线程池大小

## 测试验证

### 1. 单元测试
- `SimpleThreadLocalTest`: 基础功能测试
- `ThreadLocalTestApplicationTests`: 集成测试

### 2. 演示程序
- `ThreadLocalDemo`: 完整功能演示
- 包含基本使用、线程池、异步任务等场景

### 3. API测试
- 提供完整的REST API进行功能验证
- 支持自定义请求头模拟不同场景

## 扩展建议

### 1. 集成链路追踪
```java
// 集成 Zipkin 或 Jaeger
String traceId = SystemContextHolder.getRequestId();
Tracer tracer = getTracer();
Span span = tracer.buildSpan("business-operation")
    .withTag("trace.id", traceId)
    .start();
```

### 2. 集成日志框架
```java
// 使用 MDC 增强日志
MDC.put("userId", SystemContextHolder.getUserId());
MDC.put("requestId", SystemContextHolder.getRequestId());
MDC.put("clientIp", SystemContextHolder.getClientIp());
```

### 3. 添加监控指标
```java
// 记录上下文相关的监控指标
MeterRegistry meterRegistry = getMeterRegistry();
Counter.builder("user.requests")
    .tag("userId", SystemContextHolder.getUserId())
    .tag("clientIp", SystemContextHolder.getClientIp())
    .register(meterRegistry)
    .increment();
```

## 总结

这个模块提供了一个完整的 TransmittableThreadLocal 使用示例，展示了如何：

1. **正确使用 TransmittableThreadLocal** 在线程池场景下传递上下文
2. **自动提取请求信息** 并设置到上下文中
3. **在异步任务中保持上下文** 的连续性
4. **提供完整的测试和演示** 验证功能正确性

通过这个模块，开发者可以快速理解和应用 TransmittableThreadLocal 来解决线程池中的上下文传递问题，提升系统的可观测性和可维护性。 