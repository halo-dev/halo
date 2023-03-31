<p align="center">
    <a href="https://halo.run" target="_blank" rel="noopener noreferrer">
        <img width="100" src="https://halo.run/logo" alt="Halo logo" />
    </a>
</p>

<p align="center"><b>Halo</b> [ˈheɪloʊ]，强大易用的开源建站工具。</p>

<p align="center">
<a href="https://github.com/halo-dev/halo/releases"><img alt="GitHub release" src="https://img.shields.io/github/release/halo-dev/halo.svg?style=flat-square&include_prereleases" /></a>
<a href="https://hub.docker.com/r/halohub/halo"><img alt="Docker pulls" src="https://img.shields.io/docker/pulls/halohub/halo?style=flat-square" /></a>
<a href="https://github.com/halo-dev/halo/commits"><img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/halo-dev/halo.svg?style=flat-square" /></a>
<a href="https://github.com/halo-dev/halo/actions"><img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/halo-dev/halo/halo.yaml?branch=main&style=flat-square" /></a>
<a href="https://codecov.io/gh/halo-dev/halo"><img alt="Codecov percentage" src="https://img.shields.io/codecov/c/github/halo-dev/halo/main?style=flat-square&token=YsRUg9fall"/></a>
<br />
<a href="https://halo.run">官网</a>
<a href="https://docs.halo.run">文档</a>
<a href="https://bbs.halo.run">社区</a>
<a href="https://gitee.com/halo-dev">Gitee</a>
<a href="https://t.me/halo_dev">Telegram 频道</a>
</p>

------------------------------

## 快速开始

```bash
docker run \
  -it -d \
  --name halo \
  -p 8090:8090 \
  -v ~/.halo2:/root/.halo2 \
  halohub/halo:2.4 \
  --halo.external-url=http://localhost:8090/ \
  --halo.security.initializer.superadminusername=admin \
  --halo.security.initializer.superadminpassword=P@88w0rd
```

以上仅作为体验使用，详细部署文档请查阅：<https://docs.halo.run/getting-started/install/docker-compose>

## 在线体验

- 环境地址：<https://demo.halo.run>
- 后台地址：<https://demo.halo.run/console>
- 用户名：`demo`
- 密码：`P@ssw0rd123..`

## 生态

可访问 [awesome-halo](https://github.com/halo-sigs/awesome-halo) 查看已经适用于 Halo 2.0 的主题和插件，以及适用于 Halo
1.x 的相关仓库。

## 许可证

[![license](https://img.shields.io/github/license/halo-dev/halo.svg?style=flat-square)](https://github.com/halo-dev/halo/blob/master/LICENSE)

Halo 使用 GPL-v3.0 协议开源，请遵守开源协议。

## 贡献

参考 [CONTRIBUTING](https://github.com/halo-dev/halo/blob/master/CONTRIBUTING.md)。

<a href="https://github.com/halo-dev/halo/graphs/contributors"><img src="https://opencollective.com/halo/contributors.svg?width=890&button=false" /></a>

## 状态

![Repobeats analytics](https://repobeats.axiom.co/api/embed/ad008b2151c22e7cf734d2688befaa795d593b95.svg "Repobeats analytics image")
