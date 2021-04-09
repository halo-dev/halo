<h1 align="center"><a href="https://github.com/halo-dev" target="_blank">halo-admin</a></h1>

> halo-admin 是 [Halo](https://github.com/halo-dev/halo) 的管理端项目。

<p align="center">
<a href="https://www.npmjs.com/package/halo-admin"><img alt="npm release" src="https://img.shields.io/npm/v/halo-admin?style=flat-square"/></a>
<a href="https://www.jsdelivr.com/package/npm/halo-admin"><img alt="npm release" src="https://data.jsdelivr.com/v1/package/npm/halo-admin/badge"/></a>
<a href="https://github.com/halo-dev/halo-admin/releases"><img alt="GitHub All Releases" src="https://img.shields.io/github/downloads/halo-dev/halo-admin/total.svg?style=flat-square"></a>
<a href="https://github.com/halo-dev/halo-admin/commits"><img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/halo-dev/halo-admin.svg?style=flat-square"></a>
<a href="https://travis-ci.org/halo-dev/halo-admin"><img alt="Travis CI" src="https://img.shields.io/travis/halo-dev/halo-admin.svg?style=flat-square"/></a>
</p>

------------------------------

## 部署方案

目前 Halo 的运行包内已经包含了构建好的页面，所以如果你不需要额外部署 admin，你无需做任何操作。

### 独立部署

#### 方式一

直接下载最新构建好的版本，然后部署即可。

https://github.com/halo-dev/halo-admin/releases

#### 方式二

1、克隆项目：

```bash
git clone https://github.com/halo-dev/halo-admin
```

2、检出最新版本：

```bash
git checkout v1.4.8
```

3、打包构建：

```bash
npm i

npm run build
```

最后，得到 dist 文件夹之后就可以单独部署了。
