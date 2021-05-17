<p align="center">
    <a href="https://halo.run" target="_blank" rel="noopener noreferrer">
        <img width="100" src="https://halo.run/logo" alt="Halo logo">
    </a>
</p>

> Halo 是一款现代化的个人独立博客系统，给习惯写博客的同学多一个选择。

<p align="center">
<a href="https://github.com/halo-dev/halo/releases"><img alt="GitHub release" src="https://img.shields.io/github/release/halo-dev/halo.svg?style=flat-square"/></a>
<a href="https://github.com/halo-dev/halo/releases"><img alt="GitHub All Releases" src="https://img.shields.io/github/downloads/halo-dev/halo/total.svg?style=flat-square"></a>
<a href="https://hub.docker.com/r/halohub/halo"><img alt="Docker pulls" src="https://img.shields.io/docker/pulls/halohub/halo?style=flat-square"></a>
<a href="https://github.com/halo-dev/halo/commits"><img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/halo-dev/halo.svg?style=flat-square"></a>
<a href="https://github.com/halo-dev/halo/actions"><img alt="GitHub Workflow Status" src="https://img.shields.io/github/workflow/status/halo-dev/halo/Halo%20CI?style=flat-square"/></a>
</p>

------------------------------

## 简介

**Halo** `[ˈheɪloʊ]`，一个优秀的开源博客发布应用，值得一试。

[官网](https://halo.run) | [文档](https://docs.halo.run) | [社区](https://bbs.halo.run) | [Gitee](https://gitee.com/halo-dev) | [Telegram 频道](https://t.me/halo_dev)

## 快速开始

### Fat Jar

下载最新的 Halo 运行包：

```bash
curl -L https://github.com/halo-dev/halo/releases/download/v1.4.8/halo-1.4.8.jar --output halo.jar
```

其他地址：https://docs.halo.run/install/downloads

```bash
java -jar halo.jar
```

### Docker

```bash
docker run -it -d --name halo -p 8090:8090 -v ~/.halo:/root/.halo --restart=always halohub/halo
```

详细部署文档请查阅：<https://docs.halo.run/install/index>

## 生态

| 项目                                                                         | 状态                                                                                                                                                              | 描述                                     |
| ---------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------- |
| [halo-admin](https://github.com/halo-dev/halo-admin)                         | <a href="https://www.npmjs.com/package/halo-admin"><img alt="npm release" src="https://img.shields.io/npm/v/halo-admin?style=flat-square"/></a>                   | Web 管理端 UI，已内置在主应用            |
| [halo-comment](https://github.com/halo-dev/halo-comment)                     | <a href="https://www.npmjs.com/package/halo-comment"><img alt="npm release" src="https://img.shields.io/npm/v/halo-comment?style=flat-square"/></a>               | 独立评论组件，可以非常方便的集成到主题中 |
| [halo-comment-normal](https://github.com/halo-dev/halo-comment-normal)       | <a href="https://www.npmjs.com/package/halo-comment-normal"><img alt="npm release" src="https://img.shields.io/npm/v/halo-comment-normal?style=flat-square"/></a> | 另外一款评论组件                         |
| [halo-mobile-app](https://github.com/halo-dev/halo-mobile-app)                             | 已停止维护                                                                                                                                                        | 移动端管理 APP                           |
| [tencent-cloudbase-halo](https://github.com/halo-dev/tencent-cloudbase-halo) | 无                                                                                                                                                                | 腾讯云 CloudBase 一键部署配置            |
| [halo-theme-*](https://github.com/topics/halo-theme)                         | 无                                                                                                                                                                | GitHub 上开源的 Halo 主题集合            | 

## 许可证

[![license](https://img.shields.io/github/license/halo-dev/halo.svg?style=flat-square)](https://github.com/halo-dev/halo/blob/master/LICENSE)

Halo 使用 GPL-v3.0 协议开源，请尽量遵守开源协议。

## 贡献

参考 [CONTRIBUTING](./CONTRIBUTING.md)。

## 赞助我们

> 如果 Halo 对您有帮助，不妨赞助我们

<https://docs.halo.run/zh/contribution/sponsor>

