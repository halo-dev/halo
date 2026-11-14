# Extension 内存索引快照与差量恢复设计

日期：2026-07-29
状态：待评审

## 1. 背景与问题

Halo 的自定义模型（Extension）以 JSON 整体存储在 `extensions` 表中，物理列仅有
`name VARCHAR(255) PK`、`data BLOB`、`version BIGINT`（乐观锁）。所有业务字段埋在 JSON
里，数据库层面无法为其建立索引，因此 Halo 在内存中自行实现了索引机制
（`application/src/main/java/run/halo/app/extension/index/`）：

- 索引按 Extension 类型（Class）分桶，每个类型持有若干索引：
  `ConcurrentSkipListMap<K, Set<String>>` 正排 + 反向 HashMap + null 键集合。
- 写路径在 `ReactiveExtensionClientImpl.doCreate/doUpdate` 中，DB 写入后在事务边界内
  直接更新内存索引。
- 全量构建唯一入口是 `DefaultIndicesInitializer`：监听 `SchemeAddedEvent`，单线程、
  100 条/批扫描全表插入索引。

由此产生的问题：

1. **重启后索引全部丢失**。`Index` 接口没有任何 snapshot/restore 抽象，`close()` 即
   `map.clear()`。Halo 重启依赖 SmartLifecycle 顺序同步重建，数据量大时启动被显著拉长。
2. **插件热重启更严重**。插件与核心模型共享同一个 `IndexEngine`；`SchemeManager.unregister`
   会直接清空该类型全部索引，重新注册时运行期重建。重建窗口内查询要么抛
   `IllegalArgumentException`（先 unregister 后查询），要么读到**不完整的部分索引**
   （无 ready 标记、无阻塞）。
3. **运行期状态管理缺失**。代码中 `IndexBuildState` 与 `IndexerBuiltEvent` 两个类存在
   但全仓库无引用（旧设计的残留），当前实现没有任何"索引就绪"状态：构建/重建期间
   查询无阻塞、无标记，结果正确性依赖启动顺序的隐式保证，运行期重建（插件热重启）
   则完全没有保障。
4. **DB 事务与内存索引的一致性缺口**。索引更新发生在事务边界内、DB commit 之前：
   commit 失败时 DB 回滚但索引已变更，产生幽灵条目，且该脏状态会被未来的快照持久化。
   详见第 6 章。

## 2. 目标与非目标

### 目标

- Halo 重启后索引**秒级恢复**（不再全量扫表构建）。
- 插件 stop/start 后自定义模型索引同样可快速恢复。
- 恢复期间查询行为明确：阻塞等待（带超时），不抛异常、不返回不完整结果。
- 修复 DB 事务与内存索引的一致性缺口。
- 零新增第三方依赖，不改动存储层表结构；`api/` 模块仅新增可选的索引版本号属性
  （builder 方法，向后兼容）。

### 非目标（YAGNI）

- 定期快照（shutdown/unregister 已覆盖主要场景，脏关闭由差量恢复兜底；后续可作为
  配置项追加）。
- WAL 机制（数据库本身即 source of truth，差量窄查询已承担"重放检测"职责；数据量
  再上数量级时再评估，见第 9 章）。
- 索引下推到数据库 / 更换嵌入式持久引擎（作为后续演进方向，本设计不排斥）。
- 唯一约束（如 `spec.slug`）仅由内存索引强制这一存储层固有局限。

## 3. 总体方案

在 `halo.work-dir/indices/` 下按 Extension 类型持久化索引快照文件。保存时机为
`SchemeManager.unregister`（覆盖插件 stop 与 Halo 正常关闭两条路径）；恢复时机为
`SchemeAddedEvent` 之后、全量扫库之前，先尝试加载快照，再做基于 `name, version`
窄查询的差量补偿。快照不可用（缺失/损坏/spec 变更）时退化为现有全量构建，行为不更差。

同时为每个类型引入就绪门闩（`CountDownLatch`）：恢复/构建期间查询阻塞等待（带超时），
构建完成后放行。Halo 重启场景由 SmartLifecycle 启动顺序天然保证，无需额外防护；
门闩主要覆盖插件热重启的运行期重建窗口。

## 4. 组件设计

### 4.1 新增包 `run.halo.app.extension.index.snapshot`

**`IndexSnapshotManager`**

- 负责单类型快照的保存与加载，文件路径
  `<work-dir>/indices/<type-标识>.snapshot.gz`（GZIP 压缩，JDK 自带）。
- 保存：写入临时文件后 `rename` 到目标路径，保证原子性，避免半截文件。
- 加载：校验 magic、格式版本号；按索引粒度比对 spec 指纹，返回"可直接恢复的索引"
  与"需要重建的索引"两个集合，由调用方决定恢复策略（见 4.3）。

**快照文件格式**

```
header:  magic | formatVersion | indexCount
per index:  indexName | indexFingerprint | keyType | nullKeySet | entryCount | { key -> [name, ...] }
manifest:  entryCount | { name -> version }
```

- 索引键按 keyType（String/Long/Integer/Instant/Boolean 等）序列化为规范字符串形式，
  恢复时按 keyType 解析还原。多值索引与 LabelIndex 同样以键→主键集形式存储。
- `indexFingerprint`：**按索引粒度**对该 IndexSpec（名称、keyType、unique、单/多值、
  **索引版本号**）做规范化哈希（SHA-256）。恢复时按索引名 + 指纹逐一匹配，
  未匹配/不匹配的索引单独重建，已匹配的直接恢复——避免"一处变更、全部重建"。
- 索引版本号：`IndexSpecs` builder 新增可选属性 `version`（默认 1）。indexFunc **函数体**
  的语义变更无法自动检测，开发者通过**提升版本号**显式声明，指纹随之变化触发该索引
  重建，无需修改索引名（核心模型开发遵守，插件文档需说明）。
  涉及 `api/` 模块的 additive 变更（builder 新增可选方法，向后兼容）。
- `manifest`（`name -> version` 清单）：一致性水位线，差量恢复的比对基准。

### 4.2 `DefaultIndices` / `Index` 实现层扩展（仅 application 模块内部）

- 增加 `dump()` / `restore(...)` 能力：导出/灌入各索引的键→主键集映射及 null 键集合。
  `api/` 包的改动仅限 `IndexSpecs` builder 新增可选 `version` 属性（见 4.1），
  `IndexSpec` 等既有接口的方法签名不变。
- `DefaultIndices` 新增维护 `name -> version` 映射（每行一个 Long，内存代价可忽略），
  作为快照 manifest 的来源和并发版本守卫（见 6.2）。更新顺序必须保证
  "version 映射在索引条目提交之后更新"（见 6.3 的写路径顺序约定）。

### 4.3 `DefaultIndicesInitializer` 改动

`onSchemeAddedEvent` 的处理流程改为：

1. 该类型的就绪门闩已在注册时创建（见 4.6），构建期间查询自然阻塞。
2. 尝试 `IndexSnapshotManager` 加载快照，按索引名 + 指纹匹配（见 4.1），分三级：
   - **快照不可用（缺失/损坏/格式版本不符）** → 走现有全量扫库路径（行为与今天一致）。
   - **全部索引匹配，或仅存在被删除的索引** → 灌入匹配索引与 manifest（被删索引直接
     丢弃），执行窄查询差量恢复（见 5.1），零全表 data 扫描。
   - **存在新增/修改的索引** → 灌入匹配索引与 manifest，然后做**一次合并的全表 data
     扫描**（见 5.2）：逐行计算脏索引的 indexFunc 灌入，同时完成差量比对。
3. 门闩 `countDown`，放行阻塞中的查询。

### 4.4 `DefaultSchemeManager.unregister` 改动

`indicesManager.remove(type)` 之前，先调用 `IndexSnapshotManager` 保存该类型快照。
插件 stop 与 Halo 正常关闭（`SchemeInitializer.stop()` unregister 全部）共用此钩子。

时序澄清：插件停止时 `PluginBeforeStopSyncListener` 只清理插件 yaml 中**声明的静态资源**
（`extensionNamesMapping`，如 Role、Setting 等），不删除插件自定义模型的业务数据；
这些删除走正常写路径、同步反映到索引，随后插件 context 关闭、插件代码调用
`schemeManager.unregister()` 时才保存快照，因此快照内容与 DB 最终状态一致。

### 4.5 `ExtensionStoreRepository` 改动

新增只返回 `name, version` 两列的窄投影查询（按 store 名前缀 like），不回表取 BLOB。

### 4.6 就绪门闩与查询行为

- 就绪语义为**默认可查、构建期阻塞**：`IndicesManager.add` 之后索引即视为就绪（空索引
  是一致的）；`DefaultIndicesInitializer` 构建/恢复前调用 `markBuilding(type)`（创建
  `CountDownLatch`），完成后 `markReady(type)`（`countDown` 并移除）。`awaitReady`
  仅在门闩存在时阻塞。不引入状态枚举，门闩即"构建中"语义本身。
  旧设计残留的无引用类 `IndexBuildState`、`IndexerBuiltEvent` 已移除。
  （注：早期设计为"注册时创建门闩、构建后放行"，后改为当前语义——`add` 与构建分属
  两个调用方时，隐式的"未就绪"契约过于脆弱，直接使用 `IndicesManager` 的调用方
  会被无端阻塞。）
- `DefaultIndexEngine.retrieve*` 在目标类型门闩存在时阻塞等待（超时 30s），
  超时抛出明确异常；否则正常执行。
- Halo 重启场景无需防护：核心模型在 SmartLifecycle（SCHEME phase）同步构建，早于应用
  ready 与 ApplicationRunner，启动顺序天然阻塞。门闩只覆盖插件热重启的运行期重建窗口。
- 事件顺序约束：`DefaultIndicesInitializer` 必须先于 `GcSynchronizer`（后者依赖
  `metadata.deletionTimestamp` 索引）完成，沿用现有 `SchemeAddedEvent` 监听顺序。

## 5. 恢复流程

### 5.1 窄查询差量恢复（快照全部索引匹配 / 仅删除索引）

快照灌入后执行：

1. DB 窄查询拉取该类型全部 `(name, version)`。
2. 三方比对（以 DB 为准）：
   - DB 有、manifest 无 → 按 name 回库取 data，反序列化后 `insert`。
   - 两侧都有、version 不一致 → 回库取 data，`update`。
   - manifest 有、DB 无 → 从索引 `delete`。
3. 变更行数远小于总量时，回库取 data 使用 `findByNameIn` 分批拉取。

### 5.2 合并全表扫描（存在新增/修改的索引）

需要重建的索引其 indexFunc 依赖每行 JSON，必须扫 data。合并执行：

1. 游标分批扫描该类型全部行（name, version, data）。
2. 每行：反序列化 → 计算脏索引的 indexFunc 灌入对应索引；同时比对 manifest
   （version 不一致或 manifest 缺失 → 对匹配索引一并执行 insert/update）。
3. 扫描结束后，manifest 有而 DB 无的 name → 从索引 `delete`。

一次扫描同时完成脏索引重建与差量补偿，不额外增加窄查询。

### 5.3 正确性说明

正确性不依赖 `version` 的全局单调性：比对的是"快照时刻清单"与"DB 当前状态"两个集合，
脏关闭、快照过期均可自愈；最坏情况（全部行均变更）退化为全量重建，不更差。
索引列变更场景的成本：删除 → 零全表扫描；新增/修改 → 一次全表扫描（仅计算脏索引的
indexFunc，匹配索引免于逐行重算）。

## 6. 一致性设计

### 6.1 DB 事务与索引的提交点对齐

现状：`ReactiveExtensionClientImpl.doCreate/doUpdate` 中索引更新在事务边界内、
commit 之前执行，commit 失败会产生幽灵索引条目。

改动：索引变更通过响应式 `TransactionSynchronization` 注册回调——

- **`afterCommit`**：应用索引变更（insert/update/delete）。afterCommit 在 commit 流程内
  同步触发、先于下游 onNext，因此对外仍保持"`client.update` 返回后索引立即可查"的
  读己之写语义。
- **`afterRollback`**：利用索引现有的 prepare/commit/rollback 两阶段机制做补偿回滚，
  消除幽灵条目。

### 6.2 版本守卫的幂等应用

应用索引变更时，以 `DefaultIndices` 的 `name -> version` 映射为并发守卫：仅接受
version ≥ 当前记录版本的变更，乱序/重复/过期变更直接跳过。重试与并发交错下索引收敛到
最新已提交版本。

### 6.3 快照 dump 与索引的一致性

快照 dump 与并发写并存，**不引入类型级全局锁**，靠两个顺序约定保证安全方向
（manifest 版本 ≤ 索引内容，差量恢复只可能多干活、绝不漏更新）：

1. **写路径顺序**：索引条目先提交，version 映射后更新。崩溃时 manifest 偏旧，
   差量恢复多拉一行幂等覆盖，安全。
2. **dump 顺序**：先 dump manifest，再 dump 各索引。索引 dump 起始时刻不早于
   manifest 定型时刻，因此 dump 出的配对必然满足安全方向：
   - 期间并发插入/更新：索引 dump 可能带到更新版本而 manifest 未记录 → 差量比对发现
     不一致 → 幂等覆盖；
   - 期间并发删除：manifest 有、索引 dump 无 → 差量路径"manifest 有 DB 无 → delete"
     → no-op。
3. **依赖的数据结构事实**：`ConcurrentSkipListMap`/`ConcurrentHashMap` 迭代器为
   weakly consistent——保证完整遍历"迭代器创建时已存在"的元素恰好一次，并可能反映
   创建后的修改。dump 迭代器在 manifest 定型之后创建，故"manifest 时刻存在且未被删除的
   条目保证出现在索引 dump 中"，安全方向闭合。

反例警示（为何顺序 2 必须如此）：若先 dump 索引后 dump manifest，并发写可使快照出现
"manifest 超前于索引"（如 manifest=v2、索引=v1），差量比对会误判一致而跳过重取，
导致旧数据永久残留。

如需"精确时间点快照"可追加类型级 `ReadWriteLock`（写操作持读锁、dump 持写锁），
成本可忽略，但对正确性非必需，当前不做。

### 6.4 启动差量比对作为最终安全网

任何运行期漂移（包括极端情况下 6.1/6.2 未兜住的）在重启时由第 5 章的差量比对修复。
前提不变式：manifest 的 version 与索引内容同源、同步更新。

### 6.5 已知的毫秒级窗口

DB commit 成功到 afterCommit 应用索引之间存在极小窗口，窗口内查询可能读不到刚提交的
行；窗口内崩溃由 6.4 兜底。对 CMS 场景可接受。

## 7. 错误处理与兜底

|         场景          |                   行为                    |
|---------------------|-----------------------------------------|
| 快照文件不存在             | 全量构建（现状行为）                              |
| 快照损坏 / magic、格式版本不符 | 记 warn 日志，删除坏文件，全量构建                    |
| 部分索引指纹不匹配（新增/修改索引列） | 记 info 日志，匹配索引从快照恢复，脏索引走合并全表扫描重建（见 5.2） |
| 索引列仅被删除             | 匹配索引从快照恢复，被删索引丢弃，零全表扫描                  |
| 差量恢复中回库取数失败         | 记 error，该类型回退全量构建                       |
| 恢复期间查询              | 阻塞等待至构建完成或超时（默认 30s）后抛明确异常              |
| 快照保存失败（磁盘满等）        | 记 error，不影响关闭流程；下次启动全量构建                |

## 8. 测试计划

- 快照序列化/反序列化 round-trip：各 keyType、单值/多值索引、LabelIndex、null 键、
  空索引。
- 差量恢复：新增/更新/删除三类变更分别验证；零变更时零回库取数。
- 索引列变更：仅删除 → 零全表扫描且结果正确；新增/修改 → 匹配索引恢复 + 脏索引经合并
  扫描重建且结果正确；索引版本号提升 → 该索引指纹变化、触发重建；版本号缺省为 1 时
  行为与无版本号一致（向后兼容）。
- 快照损坏 / magic、格式版本不符 → 全量构建。
- 就绪门闩：构建期间查询阻塞、构建完成后放行、超时抛异常。
- 并发 dump 一致性：dump 期间并发插入/更新/删除，恢复后索引与 DB 比对无遗漏
  （验证 6.3 的两个顺序约定）。
- 事务一致性：模拟 commit 失败验证 afterRollback 补偿消除幽灵条目；乱序版本变更被
  版本守卫跳过。
- 集成测试：写入数据 → unregister（存快照）→ 重新 register → 断言查询结果与重启前一致
  （核心模型与模拟插件模型各一组）。

## 9. 演进选项（本设计不做，但保留挂点）

- **定期快照**：`IndexSnapshotManager` 增加调度入口即可，配置项控制开关与间隔。
- **WAL**：仅当单类型数据量达到千万级、`name, version` 窄查询成为可观测瓶颈时评估；
  届时 WAL 只是"找出变更行"的更快实现，清单格式与恢复流程不变。更便宜的先行优化：
  DB 侧覆盖索引、并行分段扫描。
- **索引下推数据库 / 嵌入式持久引擎**：快照抽象与 `dump/restore` 接口不排斥将来替换
  索引存储介质。

