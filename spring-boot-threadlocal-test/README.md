# Spring Boot ThreadLocal 测试模块

这个模块演示了如何使用 `TransmittableThreadLocal` 来完善记录系统上下文，包括操作IP、用户信息等。

## 功能特性

- 使用 `TransmittableThreadLocal` 在线程间传递上下文信息
- 自动从HTTP请求中提取客户端IP、用户代理等信息
- 支持异步任务中的上下文传递
- 提供完整的REST API接口进行测试

## 核心组件

### 1. SystemContext
系统上下文类，存储以下信息：
- 客户端IP地址
- 用户ID和用户名
- 请求ID（用于链路追踪）
- 请求路径和方法
- 用户代理信息
- 请求时间

### 2. SystemContextHolder
使用 `TransmittableThreadLocal` 的上下文持有者，提供：
- 线程安全的上下文存储
- 在线程池场景下的上下文传递
- 便捷的getter/setter方法

### 3. SystemContextInterceptor
HTTP请求拦截器，自动：
- 从请求中提取客户端IP
- 设置请求ID
- 记录请求路径和方法
- 初始化系统上下文

### 4. AsyncService
异步服务类，演示：
- 在线程池中使用上下文信息
- 异步任务中的上下文传递
- 批量任务处理

## API 接口

### 1. 获取当前上下文
```bash
GET /api/threadlocal/context
```

### 2. 设置用户信息
```bash
POST /api/threadlocal/user?userId=123&username=testuser
```

### 3. 执行异步任务
```bash
POST /api/threadlocal/async-task?taskName=test-task
```

### 4. 执行批量任务
```bash
POST /api/threadlocal/batch-task?batchName=test-batch&count=10
```

### 5. 并发任务测试
```bash
POST /api/threadlocal/concurrent-tasks
```

### 6. 打印上下文到日志
```bash
POST /api/threadlocal/print-context
```

### 7. 健康检查
```bash
GET /api/threadlocal/health
```

## 使用示例

### 1. 运行演示程序（推荐）
```bash
# 直接运行演示程序，不需要启动Web服务
java -cp target/classes com.chenjie.threadlocal.demo.ThreadLocalDemo
```

### 2. 运行单元测试
```bash
# 运行简单测试（不依赖Spring上下文）
mvn test -Dtest=SimpleThreadLocalTest

# 运行完整测试（需要Spring上下文）
mvn test -Dtest=ThreadLocalTestApplicationTests
```

### 3. 启动Web应用
```bash
mvn spring-boot:run -pl spring-boot-threadlocal-test
```

### 4. 测试Web API
```bash
# 健康检查
curl http://localhost:8080/api/threadlocal/health

# 获取当前上下文
curl http://localhost:8080/api/threadlocal/context

# 设置用户信息
curl -X POST "http://localhost:8080/api/threadlocal/user?userId=123&username=testuser"

# 执行异步任务
curl -X POST "http://localhost:8080/api/threadlocal/async-task?taskName=test-task"

# 执行批量任务
curl -X POST "http://localhost:8080/api/threadlocal/batch-task?batchName=test-batch&count=5"

# 并发任务测试
curl -X POST http://localhost:8080/api/threadlocal/concurrent-tasks
```

## 自定义请求头

可以通过以下请求头来模拟不同的客户端信息：

```bash
# 设置用户信息
curl -H "X-User-ID: user123" -H "X-Username: testuser" \
     http://localhost:8080/api/threadlocal/context

# 设置请求ID
curl -H "X-Request-ID: req-12345" \
     http://localhost:8080/api/threadlocal/context

# 模拟代理服务器
curl -H "X-Forwarded-For: 192.168.1.100" \
     http://localhost:8080/api/threadlocal/context
```

## 技术要点

### TransmittableThreadLocal vs ThreadLocal

- **ThreadLocal**: 只能在当前线程中访问，无法在线程池中传递
- **TransmittableThreadLocal**: 可以在线程池场景下正确传递上下文信息

### 线程池场景下的上下文传递

```java
// 使用 TransmittableThreadLocal 可以正确传递上下文
@Async
public CompletableFuture<String> processAsyncTask(String taskName) {
    // 在新线程中仍然可以获取到上下文信息
    String clientIp = SystemContextHolder.getClientIp();
    String userId = SystemContextHolder.getUserId();
    // ...
}
```

## 注意事项

1. **上下文清理**: 确保在请求完成后清理上下文，避免内存泄漏
2. **线程安全**: TransmittableThreadLocal 本身是线程安全的
3. **性能考虑**: 避免在上下文中存储大量数据
4. **异常处理**: 在异步任务中正确处理异常，确保上下文清理 