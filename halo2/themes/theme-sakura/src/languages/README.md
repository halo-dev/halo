<h1 align="center">国际化功能说明文档</h1>

# 功能说明
目前本主题的国际化，属于前端国际化。因此与 [Halo](https://halo.run) 后台有关的文字无法使用本国际化进行处理，例如【菜单及后台设置项】。建议将菜单手动设置为其他语种。

本文档主要说明 Sakura 主题的国际化方式，便于贡献者或二次编码的开发者进行理解。

# 国际化方案

本项目所使用的国际化方案为 [i18next](https://www.i18next.com/)。这是一款非常优秀的国际化框架，建议在开发之前了解一下。

## 所用插件
- [i18next-browser-languagedetector](https://github.com/i18next/i18next-browser-languageDetector) - 语言检查插件，能够自动检测浏览器中的用户语言
- [i18next-chained-backend](https://github.com/i18next/i18next-chained-backend) - 后端链接，可以链接多个后端和缓存。本主题使用其链接了 localstorage 缓存后端与基于 webpack 的数据后端。
- [i18next-localstorage-backend](https://github.com/i18next/i18next-localstorage-backend) - 基于 localStorage 的缓存后端
- [loc-i18next](https://github.com/mthh/loc-i18next) - 对 HTML5 进行国际化的插件 

# 开发前准备

TODO

## 开发直达车

- 如果你想添加新语种，可以从 [添加新语种](#添加新语种) 开始查看 
- 如果你想编辑现有的语种，可以从 [编辑现有语种](#编辑现有语种) 开始查看
- 如果你想对 HTML 进行编辑，并对其进行国际化，可以从 [在 HTML 中使用](#在-html-中使用) 开始查看。
- 如果你想在 JavaScript 代码中进行国际化处理，可以从 [在 JavaScript 中使用](#在-javascript-中使用) 开始查看。

# 添加新语种

TODO

# 编辑现有语种


# 在 HTML 中使用

# 在 JavaScript 中使用

# 高级功能
## 自定义模板

TODO

# 作者

© [LIlGG](https://github.com/LIlGG)，基于 [MIT](./LICENSE) 许可证发行。<br>
作者及其贡献者共有版权 （[帮助维护列表](https://github.com/LIlGG/halo-theme-sakura/graphs/contributors)）。

> [lixingyong.com](https://lixingyong.com) · GitHub [@LIlGG](https://github.com/LIlGG)