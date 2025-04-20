 # 分布式多实例部署方案设计

 ## 1. 概述
 在多实例场景下，所有 Halo 服务实例共享单一数据库，并通过 Redis Streams（或其他消息总线）做跨实例事件通知，各节点订阅事件后从数据库拉取最新数据并更新分布式缓存。定期由 Leader 节点执行全量校验补漏。

 ## 2. 依赖改动
 ### 2.1 build.gradle
 - application/build.gradle
   - 增加 `spring-boot-starter-data-redis`
   - 增加 `net.javacrumbs.shedlock:shedlock-spring` 和 `shedlock-provider-redis`

 ## 3. 配置修改
 ### 3.1 属性类
 - 修改 `application/src/main/java/run/halo/app/infra/properties/HaloProperties.java`
   - 增加分布式开关 `distributed.enabled`
   - 增加 Redis 连接、Stream 名称、consumer-group、scheduler-lock 前缀配置

 ### 3.2 应用配置文件
 - 修改 `application/src/main/resources/application.yaml`（或各环境 profile）
   - 增加 Redis 连接、缓存、调度锁配置

 ## 4. 缓存改造
 - 修改或新增 `application/src/main/java/run/halo/app/infra/config/CacheConfig.java`
   - Spring Cache 切换到 RedisCacheManager
   - 定义常用实体缓存 namespace 与过期策略

 ## 5. 业务逻辑改造
 ### 5.1 发布事件
 - 修改 `application/src/main/java/run/halo/app/content/PostContentServiceImpl.java`
 - 修改 `application/src/main/java/run/halo/app/content/comment/CommentServiceImpl.java`
   - 在写数据库事务提交后，调用 `RedisStreamEventPublisher.publish(event)`

 ### 5.2 监听事件
 - 新增 `application/src/main/java/run/halo/app/infra/messaging/RedisStreamEventPublisher.java`
 - 新增 `application/src/main/java/run/halo/app/infra/messaging/RedisStreamEventListener.java`
   - 订阅 Redis Stream，消费后调用对应 Service `reload(id)`，并 `cache.evict` 或 `cache.put`

 ## 6. 分布式调度与全量校验
 - 新增 `application/src/main/java/run/halo/app/infra/scheduler/DistributedSchedulerConfig.java`
   - 配置 ShedLock 基于 Redis 的分布式锁
 - 新增定时任务 `FullDataChecksumTask.java`
   - Leader 节点每小时计算所有 Post/Comment 的版本摘要列表（ID→version），广播或写入 DB
   - 所有节点对比本地缓存，根据缺失项从 DB 拉取并更新缓存

 ## 7. 不需要修改的部分
 - `api/` 模块：数据契约、抽象接口无需改动
 - `application/src/main/java/run/halo/app/extension`：Kubernetes-style 扩展框架与插件机制
 - `ui/` 前端：Console / UC 端无需改动，直连后端 API 即可
 - 其它基础功能：安全、主题、搜索、通知、备份恢复

 ## 8. 注意事项
 - 事务与事件的一致性（可选 Outbox 模式）
 - 消息幂等与批量合并
 - Redis Stream 消费组回溯策略
 - ShedLock 锁超时与 Leader 切换

 -- End of Document --