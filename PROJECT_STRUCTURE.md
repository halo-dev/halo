# Halo 项目结构文档

## 概述

Halo 是一个开源 CMS/博客平台。技术栈：Java 21 + Spring Boot WebFlux + R2DBC（后端），Vue 3 + TypeScript + Vite + TailwindCSS（前端），Gradle（构建）。

```
halo/
├── api/                        # 公共 API 库（run.halo.app）
├── application/                # Spring Boot 主应用
├── ui/                         # 前端（pnpm workspace）
├── platform/                   # BOM（依赖版本管理）
├── api-docs/                   # OpenAPI 文档（openapi/v3_0）
├── docs/                       # 开发者文档
├── e2e/                        # 端到端测试
├── hack/                       # 开发辅助脚本
├── gradle/                     # Gradle wrapper
├── buildSrc/                   # Gradle 构建插件
├── openspec/                   # OpenSpec 变更管理
├── .github/                    # CI/CD + Issue 模板 + AI prompts
├── .claude/                    # Claude Code 配置
├── .codex/                     # Codex 配置（与 .claude/ 重复，可删除）
├── build.gradle                # 根构建脚本
├── settings.gradle             # 模块配置
├── gradle.properties           # 项目版本
├── AGENTS.md                   # AI 代理规范（完整版）
└── CLAUDE.md                   # Claude Code 项目配置
```

---

## 一、后端

### 1.1 `api/` — 公共 API 库

发布为 `run.halo.app`，供外部插件使用。定义扩展模型、服务接口、安全抽象。

```
api/src/main/java/run/halo/app/
├── core/
│   ├── extension/              # 核心扩展模型
│   │   ├── content/            #   文章、分类、标签、快照
│   │   ├── attachment/         #   附件
│   │   ├── endpoint/           #   端点
│   │   ├── notification/       #   通知
│   │   └── service/            #   服务接口
│   ├── user/                   # 用户模型
│   └── attachment/             # 附件策略接口
├── extension/                  # 扩展框架（Scheme、Watcher、Controller 等）
│   ├── controller/
│   ├── exception/
│   ├── index/query/
│   └── router/
├── theme/                      # 主题引擎 API
│   ├── dialect/
│   ├── finders/vo/
│   └── router/
├── security/
│   └── authentication/         # 认证抽象
│       ├── login/
│       └── oauth2/
├── plugin/                     # 插件 API
├── content/comment/            # 评论 API
├── notification/               # 通知 API
├── search/event/               # 搜索事件
├── migration/                  # 迁移 API
├── infra/                      # 基础设施（DataBuffer 工具等）
└── event/                      # 事件类型（post、user）
```

### 1.2 `application/` — 主应用

Spring Boot 应用，所有业务逻辑都在这里。

```
application/src/main/java/run/halo/app/
├── content/                    # 内容管理
│   ├── comment/                #   评论服务
│   ├── impl/                   #   文章/分类/标签实现
│   ├── permalinks/             #   永久链接
│   └── stats/                  #   统计
├── core/
│   ├── attachment/             #   附件（上传、缩略图、endpoint）
│   ├── counter/                #   计数器
│   ├── endpoint/console/       #   管理后台 API
│   ├── endpoint/theme/         #   主题公共 API
│   ├── endpoint/uc/            #   用户中心 API
│   ├── reconciler/             #   调和器
│   └── user/                   #   用户服务
├── extension/                  # 扩展框架实现
│   ├── controller/             #   控制器调度
│   ├── gc/                     #   垃圾回收
│   ├── index/                  #   索引
│   ├── router/                 #   路由
│   └── store/                  #   存储
├── security/                   # 安全
│   ├── authentication/         #   认证（登录、OAuth2、Token、二步验证、RememberMe）
│   ├── authorization/          #   授权
│   ├── device/                 #   设备管理
│   ├── preauth/                #   预认证
│   ├── session/                #   会话
│   └── switchuser/             #   用户切换
├── theme/                      # 主题引擎
│   ├── dialect/expression/     #   Thymeleaf 方言
│   ├── endpoint/               #   主题端点
│   ├── engine/                 #   模板引擎
│   ├── finders/impl/           #   Finder 实现
│   ├── router/factories/       #   路由工厂
│   └── service/                #   主题服务
├── plugin/                     # 插件系统（PF4J）
│   ├── event/
│   ├── extensionpoint/
│   └── resources/
├── search/                     # 全文搜索（Lucene）
├── notification/               # 通知系统
├── migration/impl/             # 数据迁移
├── infra/                      # 基础设施
│   ├── actuator/               #   监控端点
│   ├── config/                 #   Spring 配置
│   ├── exception/handlers/     #   全局异常处理
│   ├── properties/             #   配置属性
│   ├── ui/                     #   UI 资源服务
│   └── webfilter/              #   Web 过滤器
├── event/                      # 事件处理器
└── security/jackson2/          # 安全序列化
```

---

## 二、前端 `ui/`

pnpm workspace，包含管理后台 (`/console`) 和用户中心 (`/uc`) 两个应用入口。

### 2.1 顶层目录

```
ui/
├── src/                        # 共享组件和基础框架
│   ├── components/             #   通用组件（编辑器、上传、表单等）
│   ├── composables/            #   组合式函数
│   ├── formkit/                #   FormKit 表单组件
│   ├── layouts/                #   基础布局
│   ├── router/                 #   基础路由
│   ├── stores/                 #   状态管理
│   ├── styles/                 #   全局样式
│   ├── utils/                  #   工具函数
│   ├── views/exceptions/       #   异常页面
│   └── vite/                   #   Vite 插件
├── console-src/                # 管理后台入口
│   ├── components/snapshots/   #   快照组件
│   ├── composables/
│   ├── layouts/                #   后台布局
│   ├── modules/                #   业务模块
│   │   ├── contents/           #     内容管理
│   │   ├── dashboard/          #     仪表盘
│   │   ├── interface/          #     界面（主题、菜单）
│   │   └── system/             #     系统设置
│   ├── router/guards/          #   路由守卫
│   └── stores/
├── uc-src/                     # 用户中心入口
│   ├── layouts/
│   ├── modules/
│   │   ├── contents/           #     内容
│   │   ├── notifications/      #     通知
│   │   └── profile/            #     个人信息
│   └── router/guards/
├── packages/                   # 共享包（workspace 包）
│   ├── api-client/             #   自动生成的 API 客户端
│   ├── components/             #   通用 UI 组件库
│   ├── console-shared/         #   管理后台共享代码
│   ├── editor/                 #   编辑器组件
│   ├── shared/                 #   通用共享代码
│   └── ui-plugin-bundler-kit/  #   插件打包工具
├── docs/                       # 前端文档
├── scripts/                    # 构建脚本
├── public/ui-assets/           # 静态资源
├── patches/                    # npm 补丁
└── .husky/                     # Git hooks
```

---

## 三、平台 BOM `platform/`

依赖版本集中管理，分为两个 BOM：

| BOM | 路径 | 用途 |
|-----|------|------|
| Application BOM | `platform/application/` | 应用构建的共享依赖约束 |
| Plugin BOM | `platform/plugin/` | 插件开发的依赖约束 |

---

## 四、配置与工具链

| 文件/目录 | 用途 |
|---|---|
| `build.gradle` | 根构建 — `format 'misc'` 必须留在这里 |
| `settings.gradle` | 模块声明 |
| `gradle.properties` | 项目版本号 |
| `gradle/libs.versions.toml` | 依赖版本目录 |
| `buildSrc/` | 自定义 Gradle 插件（preset download 等） |
| `gradlew` / `gradlew.bat` | Gradle wrapper |
| `.editorconfig` | 编辑器配置 |
| `.gitignore` | Git 忽略规则 |
| `Dockerfile` | 容器构建 |
| `hack/cherry_pick_pull.sh` | Cherry-pick 辅助脚本 |

### CI/CD

```
.github/
├── workflows/                  # GitHub Actions 工作流
├── actions/
│   ├── docker-buildx-push/     #   Docker 构建推送
│   └── setup-env/              #   环境配置
├── ISSUE_TEMPLATE/             # Issue 模板（中英文）
└── prompts/                    # GitHub Copilot / AI prompts
```

### AI 工具配置

| 目录 | 工具 | 说明 |
|---|---|---|
| `.claude/` | Claude Code | commands + skills（openspec 工作流） |
| `.codex/` | Codex | 与 `.claude/skills/` 完全重复，**可删除** |
| `AGENTS.md` | 通用 | 完整 AI 代理规范 |
| `CLAUDE.md` | Claude Code | 精简项目配置，指向 AGENTS.md |
| `openspec/` | OpenSpec | 变更提案、规格、任务管理 |

### OpenSpec 结构

```
openspec/
├── config.yaml                 # 全局配置（schema 类型、AI 上下文）
├── specs/                      # 活跃规格
│   ├── category-post-navigation/
│   ├── signup-agreement/
│   └── theme-delete-guard/
└── changes/archive/            # 已归档变更（5 个历史记录）
```

---

## 五、常用命令速查

```bash
./gradlew build                                    # 全量构建
./gradlew spotlessApply                            # 格式化
./gradlew :application:test                        # 后端测试
./gradlew :application:bootRun                     # 启动后端
./gradlew generateOpenApiDocs                      # 生成 OpenAPI 文档
pnpm -C ui typecheck && pnpm -C ui lint            # 前端校验
pnpm -C ui test:unit                               # 前端单元测试
pnpm -C ui api-client:gen                          # 重新生成 API 客户端
```

---

## 六、关键资源位置

| 资源 | 路径 |
|---|---|
| 数据库迁移脚本 | `application/src/main/resources/db/migration/{h2,mariadb,mysql,postgresql}/` |
| 扩展类型 Schema | `application/src/main/resources/extensions/` |
| 应用配置 | `application/src/main/resources/application.yaml` |
| 环境配置 | `application/src/main/resources/application-{local,dev,postgresql}.yaml` |
| 默认工作目录 | `~/.halo2`（可通过 `halo.work-dir` 覆盖） |
