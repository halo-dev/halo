# Halo 分布式健康检查

本模块提供了对Halo分布式部署的健康检查功能，主要通过监控Redis通信来判断分布式系统的健康状况。

## 功能特点

- 自动检测分布式模式是否启用
- 监控Redis连接和通信状态
- 完全集成到Spring Boot Actuator健康检查系统
- 提供详细的健康状态信息

## 使用方法

1. 健康检查功能已自动集成到应用中，无需额外配置即可启用。

2. 分布式健康检查仅在`halo.distributed.enabled=true`时执行，否则将跳过Redis检查。

3. 要查看健康检查结果，可以访问Actuator健康端点：
   ```
   GET /actuator/health
   ```

4. 要查看分布式子系统的详细健康状态：
   ```
   GET /actuator/health/distributed
   ```

## 健康检查响应示例

### 分布式模式启用且健康
```json
{
  "status": "UP",
  "components": {
    "distributedHealthIndicator": {
      "status": "UP",
      "details": {
        "distributedMode": "enabled",
        "redisConnection": "healthy"
      }
    }
  }
}
```

### 分布式模式启用但不健康
```json
{
  "status": "DOWN",
  "components": {
    "distributedHealthIndicator": {
      "status": "DOWN",
      "details": {
        "distributedMode": "enabled",
        "redisConnection": "unhealthy",
        "error": "Redis communication failure"
      }
    }
  }
}
```

### 分布式模式未启用
```json
{
  "status": "UP",
  "components": {
    "distributedHealthIndicator": {
      "status": "UP",
      "details": {
        "distributedMode": "disabled"
      }
    }
  }
}
```

## 集成到监控系统

此健康检查可以轻松集成到常见的监控系统，如Prometheus + Grafana、Datadog或ELK等。

如需将健康检查结果暴露为Prometheus指标，请启用Spring Boot的Prometheus集成：

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
  endpoints:
    web:
      exposure:
        include: "health,prometheus"
```

然后可以通过`/actuator/prometheus`端点获取指标数据。
