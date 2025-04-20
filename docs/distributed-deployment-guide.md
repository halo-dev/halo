# Halo 分布式多实例部署指南

本文档提供了 Halo 2.20.18+ 分布式多实例部署的详细配置说明。通过分布式部署，您可以在多台服务器上运行 Halo 实例，提高系统的可用性和性能。

## 1. 前提条件

- 多台服务器，每台都部署有 Halo 2.20.18 或更高版本
- 共享的数据库（MySQL/PostgreSQL）
- Redis 服务器实例（用于分布式缓存、消息和锁）
- 共享的附件存储（可选，但推荐）

## 2. 配置步骤

### 2.1 添加Redis依赖（自行构建时）

如果您是从源代码构建 Halo，请确保在 `application/build.gradle` 中包含以下依赖：

```gradle
dependencies {
    // Redis for distributed messaging and caching
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    // ShedLock for distributed scheduling
    implementation 'net.javacrumbs.shedlock:shedlock-spring'
    implementation 'net.javacrumbs.shedlock:shedlock-provider-redis'
}
```

### 2.2 配置 application.yaml

在每个 Halo 实例的 `application.yaml` 中添加以下配置：

```yaml
spring:
  # Redis 连接配置
  data:
    redis:
      host: ${REDIS_HOST:your-redis-host}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:your-redis-password}
      database: ${REDIS_DATABASE:0}
      timeout: 10000
  # 配置缓存类型
  cache:
    type: ${CACHE_TYPE:redis}  # 非分布式模式可使用 caffeine
    redis:
      time-to-live: 3600000
      key-prefix: halo:

# Halo 分布式配置
halo:
  distributed:
    # 启用分布式部署功能
    enabled: true
    # Redis Stream 名称
    stream-key: halo:distributed:stream
    # 消费组名称
    consumer-group: halo-consumer-group
    # 调度锁前缀
    scheduler-lock-prefix: halo:scheduler-lock:
    # 监听器轮询间隔（毫秒）
    listener-interval: 1000
```

### 2.3 配置共享数据库

所有实例必须共享同一个数据库。以 MySQL 为例：

```yaml
spring:
  r2dbc:
    url: r2dbc:mysql://your-db-host:3306/halo?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: your-username
    password: your-password
```

### 2.4 配置共享附件存储（可选但推荐）

建议使用对象存储（如 S3、OSS 等）或 NFS 共享存储，以确保多实例间附件的一致性。

## 3. 运行与维护

### 3.1 启动实例

配置完成后，按常规方式启动各 Halo 实例：

```bash
java -jar halo.jar --spring.config.location=/path/to/your/application.yaml
```

或使用 Docker 启动：

```bash
docker run -d --name halo -p 8090:8090 \
  -v ~/.halo2:/root/.halo2 \
  -e REDIS_HOST=your-redis-host \
  -e REDIS_PORT=6379 \
  -e REDIS_PASSWORD=your-redis-password \
  -e CACHE_TYPE=redis \
  -e HALO_DISTRIBUTED_ENABLED=true \
  halohub/halo:2.20
```

### 3.2 负载均衡配置

通过 Nginx 或其他负载均衡器在前端对多个 Halo 实例进行负载均衡：

```nginx
upstream halo {
    server halo-instance-1:8090;
    server halo-instance-2:8090;
    # ...更多实例
}

server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        proxy_pass http://halo;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 3.3 会话持久化

分布式部署时，用户会话已自动通过 Redis 共享，无需额外配置。

### 3.4 监控

可通过 Spring Boot Actuator 监控各个实例的状态：

```
http://<halo-instance>:8090/actuator/health
```

## 4. 工作原理

Halo 分布式部署基于以下几个关键机制：

1. **共享数据库**：所有实例共享同一数据源，保证数据一致性基础
2. **Redis 缓存**：利用 Redis 实现分布式缓存，避免本地缓存不一致问题
3. **事件通知**：通过 Redis Streams 实现跨实例事件广播，触发缓存更新
4. **分布式锁**：使用 ShedLock 实现定时任务的分布式调度，避免任务重复执行
5. **全量校验**：定期由主节点执行全量校验，确保各节点缓存数据一致

## 5. 常见问题与解决方案

### 5.1 会话丢失问题

**症状**：用户需要频繁重新登录
**解决**：确认 Redis 配置正确，且 Redis 连接稳定

### 5.2 数据不一致问题

**症状**：在不同实例看到的数据不一致
**解决**：检查 Redis Stream 是否正常工作，可通过 Redis CLI 查看:
```
XINFO STREAM halo:distributed:stream
XINFO GROUPS halo:distributed:stream
```

### 5.3 定时任务重复执行

**症状**：定时任务在多个实例上同时执行
**解决**：确认 ShedLock 配置正确，Redis 连接稳定

## 6. 性能优化建议

1. 针对 Redis 的优化：
   - 配置适当的连接池参数
   - 监控 Redis 内存使用情况
   - 对高频读取的数据使用本地缓存前置

2. 针对负载均衡的优化：
   - 使用会话亲和（sticky session）提高缓存命中率
   - 监控各实例负载，按需调整实例数量

## 7. 限制与注意事项

1. 分布式部署方案主要适用于博客、内容站，流量和用户较多的场景
2. Redis 成为系统稳定性的关键，建议使用高可用 Redis 集群
3. 系统配置和主题配置的更改需要小心，可能需要所有节点重启

---

如有更多问题，请参考 Halo 官方文档或在社区中提问。