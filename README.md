<p align="center">
    <a href="https://halo.run" target="_blank" rel="noopener noreferrer">
        <img width="100" src="https://halo.run/logo" alt="Halo logo" />
    </a>
</p>

<p align="center"><b>Halo</b> [ˈheɪloʊ]，一款现代化的开源博客/CMS系统，值得一试。</p>

<p align="center">
<a href="https://github.com/halo-dev/halo/releases"><img alt="GitHub release" src="https://img.shields.io/github/release/halo-dev/halo.svg?style=flat-square&include_prereleases" /></a>
<a href="https://github.com/halo-dev/halo/releases"><img alt="GitHub All Releases" src="https://img.shields.io/github/downloads/halo-dev/halo/total.svg?style=flat-square" /></a>
<a href="https://hub.docker.com/r/halohub/halo"><img alt="Docker pulls" src="https://img.shields.io/docker/pulls/halohub/halo?style=flat-square" /></a>
<a href="https://github.com/halo-dev/halo/commits"><img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/halo-dev/halo.svg?style=flat-square" /></a>
<a href="https://github.com/halo-dev/halo/actions"><img alt="GitHub Workflow Status" src="https://img.shields.io/github/workflow/status/halo-dev/halo/Halo%20CI?style=flat-square" /></a>
<br />
<a href="https://halo.run">官网</a>
<a href="https://docs.halo.run/2.0.0-SNAPSHOT">文档（2.0 Alpha）</a>
<a href="https://bbs.halo.run">社区</a>
<a href="https://gitee.com/halo-dev">Gitee</a>
<a href="https://t.me/halo_dev">Telegram 频道</a>
</p>

------------------------------

## 注意

当前分支为 Halo 2.0 的开发分支，目前 Halo 2.0 处于 Alpha 测试阶段，无法从 1.5 直接升级，也不建议在生产环境使用。稳定版本（Halo 1.x）请查阅以下地址：

- 1.5 分支：<https://github.com/halo-dev/halo/tree/release-1.5>
- 1.6 分支：<https://github.com/halo-dev/halo/tree/release-1.6>
- 1.5 文档：<https://docs.halo.run>

## 快速开始

### Docker

```bash
docker run -it -d --name halo-next -p 8090:8090 -v ~/halo-next:/root/halo-next --restart=unless-stopped halohub/halo-dev:2.0.0-alpha.1
```

详细部署文档请查阅：<https://docs.halo.run/next/getting-started/install/linux>

## 生态

可访问 [awesome-halo](https://github.com/halo-sigs/awesome-halo) 查看已经适用于 Halo 2.0 的主题和插件，以及适用于 Halo 1.x 的相关仓库。

## 许可证

[![license](https://img.shields.io/github/license/halo-dev/halo.svg?style=flat-square)](https://github.com/halo-dev/halo/blob/master/LICENSE)

Halo 使用 GPL-v3.0 协议开源，请遵守开源协议。

## 贡献

参考 [CONTRIBUTING](https://github.com/halo-dev/halo/blob/master/CONTRIBUTING.md)。

<a href="https://github.com/halo-dev/halo/graphs/contributors"><img src="https://opencollective.com/halo/contributors.svg?width=890&button=false" /></a>

## 状态

![Repobeats analytics](https://repobeats.axiom.co/api/embed/ad008b2151c22e7cf734d2688befaa795d593b95.svg "Repobeats analytics image")
