# Halo — CLAUDE.md

Open-source CMS/blogging platform。技术栈：Java 21 / Spring Boot WebFlux + R2DBC + Vue 3 + TypeScript + TailwindCSS。

版本号统一管理在 `gradle/libs.versions.toml`、`gradle.properties` 和 `ui/package.json`，不要硬编码。

## 关键约定

- 始终从仓库根目录执行命令，不要分别进入子目录操作
- 前后端变更常常属于同一个任务——把仓库当全栈项目，不要当作两个独立项目
- **必须同时阅读 `AGENTS.md`**——它包含完整的模块划分、嵌套 AGENTS.md 清单、关键陷阱（如 H2 路径必须绝对路径、`format 'misc'` 不能移出根 build.gradle）
- 不要 `git add -A` 或 `git add .`——application/ 下可能出现 H2 产物和构建输出，只能按文件分别 stage

## 常用命令

```bash
./gradlew build                                # 全量构建
./gradlew spotlessApply                        # 格式化 Java 和 markdown/json/properties
./gradlew :application:test                    # 后端测试
./gradlew :application:bootRun                 # 启动后端
pnpm -C ui typecheck && pnpm -C ui lint        # 前端校验
pnpm -C ui test:unit                           # 前端单元测试
./gradlew generateOpenApiDocs && pnpm -C ui api-client:gen  # 刷新 OpenAPI 生成的客户端
```

## 注意

- 数据库迁移用 r2dbc-migrate（不是 Flyway/Liquibase）
- 虚拟线程已启用，不要在响应式路径中引入阻塞 I/O
- 不要直接编辑 `ui/packages/api-client/src/` 中的生成文件
- 提交前务必跑 `./gradlew spotlessApply` 和相关测试
