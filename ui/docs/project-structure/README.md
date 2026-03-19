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
├── console.html
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
├── uc.html
└── vite.config.ts                      # Console 和 User Center 共用的 Vite 配置
```

可以注意到 Console 和 User Center 仅仅只是使用源码目录和多页面入口进行区分，本质上还是同一个项目。

## 开发环境访问方式

开发环境下只启动一个 Vite Dev Server，默认端口为 `3000`。

开发时应通过后端访问：

- `http://localhost:8090/console`
- `http://localhost:8090/uc`

这是因为后端在开发环境中会根据 `application/src/main/resources/application-dev.yaml` 中的 `halo.ui.proxy.*` 配置，将 `/console/**` 和 `/uc/**` 的 HTML 页面请求代理到 `http://localhost:3000/`。

不能直接使用 `http://localhost:3000/console` 或 `http://localhost:3000/uc` 访问页面，因为前端运行后发起的后端 API 请求会产生跨域问题。

需要注意的是，开发环境下后端只代理页面入口，不代理静态资源路径。页面中的脚本和样式资源仍然由 Vite Dev Server 直接提供，但页面入口本身应始终从 Halo 后端地址进入。

## 构建产物

构建时，Console 和 User Center 会通过多页面模式生成到同一个产物目录：

```bash
build/dist/ui
├── console.html
├── uc.html
└── ui-assets/
```

随后后端构建过程会将这些文件复制到应用资源目录，生产环境中的访问方式为：

- `/console/**` 返回 `ui/console.html`
- `/uc/**` 返回 `ui/uc.html`
- `/ui-assets/**` 提供前端静态资源
