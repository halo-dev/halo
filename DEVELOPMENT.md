# Halo 开发指南

本文档面向 Halo 核心开发者，介绍如何设置开发环境并进行调试。

## 环境要求

- JDK 17+
- Gradle 8.0+
- Node.js 18+
- pnpm 8+
- PostgreSQL 15+ (可选，默认使用 H2)

## 快速开始

### 1. 克隆仓库

```bash
git clone https://github.com/halo-dev/halo.git
cd halo
git submodule init
git submodule update
```

### 2. 启动后端

```bash
./gradlew bootRun --args="--spring.profiles.active=dev"
```

后端服务将启动在 `http://localhost:8090`

### 3. 启动前端

```bash
cd ui
pnpm install
pnpm dev
```

前端开发服务器将启动在 `http://localhost:3000`

### 4. 访问应用

打开浏览器访问 `http://localhost:3000`，按照初始化向导完成设置。

## 开发模式配置

### 使用 PostgreSQL (推荐)

```bash
./gradlew bootRun --args="--spring.profiles.active=dev,postgresql"
```

配置 `application-dev.yaml`:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/halo
    username: halo
    password: halo
```

### 热加载

使用 Gradle 的 continuous build 模式：

```bash
./gradlew bootRun --continuous
```

或使用 IDEA 的 Spring Boot DevTools。

## 调试技巧

### 后端调试

1. 在 IDEA 中设置断点
2. 使用 Debug 模式运行 `Application`
3. 或使用 Gradle 命令：
   ```bash
   ./gradlew bootRun --debug-jvm
   ```

### 前端调试

1. 浏览器打开 `http://localhost:3000`
2. 打开开发者工具 (F12)
3. 使用 Vue DevTools 扩展

### API 调试

推荐工具：
- [Postman](https://www.postman.com/)
- [Insomnia](https://insomnia.rest/)
- [HTTPie](https://httpie.io/)

API 文档：`http://localhost:8090/swagger-ui.html`

## 代码规范

### 后端

- 遵循 Google Java Style Guide
- 使用 IDEA 自动格式化 (Ctrl+Alt+L)
- 单元测试覆盖率 > 60%

### 前端

- ESLint + Prettier 自动格式化
- Vue 3 Composition API 风格
- TypeScript 严格模式

### 提交规范

```
type(scope): description

[optional body]

[optional footer]
```

Types:
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具相关

## 常见问题

### Q: 前端请求 404

确保后端已启动，并检查 `ui/.env.development` 中的代理配置。

### Q: 数据库连接失败

检查数据库服务是否启动，以及连接配置是否正确。

### Q: 编译失败

尝试清理构建缓存：
```bash
./gradlew clean
rm -rf ui/node_modules
```

### Q: 如何重置数据

删除工作目录：
```bash
rm -rf ~/.halo2
```

## 贡献流程

1. Fork 仓库
2. 创建特性分支: `git checkout -b feature/my-feature`
3. 提交代码: `git commit -m "feat: add new feature"`
4. 推送分支: `git push origin feature/my-feature`
5. 创建 Pull Request

## 资源链接

- [官方文档](https://docs.halo.run)
- [API 文档](https://api.halo.run)
- [开发者论坛](https://bbs.halo.run)
- [插件开发指南](https://docs.halo.run/developer-guide/plugin/prepare)

## 获取帮助

- GitHub Issues: https://github.com/halo-dev/halo/issues
- 社区论坛: https://bbs.halo.run
- Telegram: https://t.me/halo_dev

---

感谢你对 Halo 的贡献！🎉
