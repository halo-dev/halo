## README

<p align="center">
    <a href="https://halo.run" target="_blank" rel="noopener noreferrer">
        <img width="100" src="https://halo.run/logo" alt="Halo logo" />
    </a>
</p>

> Halo 2.0 的管理端项目（原 halo-admin）

<p align="center">
<a href="https://github.com/halo-dev/console/releases"><img alt="GitHub release" src="https://img.shields.io/github/release/halo-dev/console.svg?style=flat-square" /></a>
<a href="https://github.com/halo-dev/console/blob/master/LICENSE"><img alt="GitHub" src="https://img.shields.io/github/license/halo-dev/console?style=flat-square"></a>
<a href="https://github.com/halo-dev/console/commits"><img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/halo-dev/console.svg?style=flat-square"></a>
<a href="https://github.com/halo-dev/console/actions"><img alt="GitHub Workflow Status" src="https://img.shields.io/github/workflow/status/halo-dev/console/Halo%20Admin%20CI?style=flat-square"/></a>
<a href="https://gitpod.io/#https://github.com/halo-dev/console"><img alt="Gitpod ready-to-code" src="https://img.shields.io/badge/Gitpod-ready--to--code-blue?logo=gitpod&style=flat-square"/></a>
</p>

------------------------------

## 注意

当前分支为 Halo 2.0 的 Console 端开发分支，目前 Halo 2.0 处于 Alpha 测试阶段，不建议从 1.5 直接升级，也不建议在生产环境使用。Console 端稳定版本（Halo 1.5）请查阅以下地址：

- 1.5 分支：<https://github.com/halo-dev/console/tree/release-1.5>
- 1.6 分支：<https://github.com/halo-dev/console/tree/release-1.6>

> 当前仓库已经将 `halo-admin` 改为了 `console`。但对于 Halo 1.x 版本，依旧保持 halo-admin 的概念。

## 开发环境运行

```bash
# pnpm@7.0.0+
npm install -g pnpm
```

```bash
pnpm install 
```

```bash
pnpm build:packages
```

```bash
pnpm dev
```

## 生产构建

```bash
pnpm build
```

## 状态

![Repobeats analytics](https://repobeats.axiom.co/api/embed/9ae12e8e0b9ed7df1b5364169186544d89c1c6bc.svg "Repobeats analytics image")
