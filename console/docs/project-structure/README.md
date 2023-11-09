# 项目结构

此目录为 Halo 前端项目的根目录，包含 Console 和 User Center 两部分。

## 名词解释

- Console：管理控制台，主要包含内容管理、系统管理、插件、主题等面向管理员的功能。
- User Center：用户中心，主要面向单个用户。

## 目录结构

```bash
├── console-src                         # Console 部分的源码
│   ├── composables
│   ├── layouts
│   ├── modules
│   ├── router
│   ├── stores
│   ├── styles
│   ├── views
│   ├── App.vue
│   └── main.ts
├── packages                            # 公共库，会在 Halo 发布版本的时候发布到 npmjs.com
│   ├── api-client                      # 根据 OpenAPI 生成的 API 客户端
│   ├── components                      # 基础组件库
│   └── shared                          # 共享库，主要提供给插件使用
├── src                                 # Console 和 User Center 共享的源码
│   ├── assets
│   ├── components
│   ├── constants
│   ├── formkit
│   ├── locales
│   ├── setup
│   ├── stores
│   ├── types
│   ├── utils
│   └── vite
├── uc-src                              # User Center 部分的源码
│   ├── router
│   ├── App.vue
│   └── main.ts
├── env.d.ts
├── index.html
├── package.json
├── pnpm-lock.yaml
├── pnpm-workspace.yaml
├── postcss.config.js
├── prettier.config.js
├── tailwind.config.js
├── tsconfig.app.json
├── tsconfig.json
├── tsconfig.node.json
├── tsconfig.vitest.json
├── vite.config.ts                      # Console 的 Vite 配置，使用 `--config` 指定
├── vite.uc.config.ts                   # User Center 的 Vite 配置，使用 `--config` 指定
└── vitest.config.ts
```

可以注意到 Console 和 User Center 仅仅只是使用了目录和 Vite 配置进行区分，本质上还是同一个项目，启动 Dev Server 的时候会同时启动两个 Vite 服务，路径分别为 `/console` 和 `/uc`。

同时，在构建时，会将 Console 和 User Center 两部分分别构建为两个独立的项目，构建后的文件会分别放在后端的 `application/src/main/resources/console` 和 `application/src/main/resources/uc` 目录下，最终通过 Halo 本身进行托管。
