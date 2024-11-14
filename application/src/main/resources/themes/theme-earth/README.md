# Earth

Halo 2.0 的默认主题。

![Earth](./screenshot.jpg)

## 使用方式

1. 手动从以下地址下载主题包并在 Console 的主题管理界面安装，安装方式可参考：<https://docs.halo.run/user-guide/themes>

    - https://www.halo.run/store/apps/app-KgWqR
    - https://github.com/halo-dev/theme-earth/releases

3. 如果安装了[应用市场](https://www.halo.run/store/apps/app-VYJbF)插件，可以直接在应用市场中搜索`Earth`并安装。

## 插件支持

Earth 主题支持以下 Halo 插件：

- 友情链接（/links）：<https://halo.run/store/apps/app-hfbQg>
- 图库（/photos）：<https://halo.run/store/apps/app-BmQJW>
- 瞬间（/moments）：<https://halo.run/store/apps/app-SnwWD>

为了获得更好的体验，你还可以安装以下插件（如果需要）：

- highlight.js 代码高亮：<https://halo.run/store/apps/app-sqpgf>
- lightgallery.js 灯箱：<https://halo.run/store/apps/app-OoggD>

## 开发

```bash
git clone git@github.com:halo-dev/theme-earth.git ~/halo2-dev/themes/theme-earth
```

```bash
cd ~/halo2-dev/themes/theme-earth
```

```bash
pnpm install 
```

```bash
pnpm dev
```

主题开发文档可查阅：<https://docs.halo.run/2.0.0-SNAPSHOT/developer-guide/theme/prepare>

## 构建

> 如果你使用的是 Windows 操作系统，请安装 `make` 命令并在 Git Bash 或 WSL 中执行。

```bash
make build
```

然后将 `dist` 目录压缩成 `ZIP` 格式压缩包即可在 Halo 后台上传安装。
