<h1><a href="#" target="_blank">Halo</a></h1>

> Halo may be the best Java blog system. | Halo可能是最好的Java博客系统。

[![JDK](https://img.shields.io/badge/JDK-1.8-yellow.svg)](#)
[![GitHub release](https://img.shields.io/github/release/ruibaby/halo.svg)](https://github.com/ruibaby/halo/releases)
[![Travis CI](https://img.shields.io/travis/ruibaby/halo.svg)](https://travis-ci.org/ruibaby/halo)

------------------------------

## 目录

- [Introduction 简介](#introduction-简介)
- [Quickstart 快速开始](#quickstart-快速开始)
- [Demo 演示](#demo-演示)
- [Download 下载部署](#download-下载部署)
- [Docs 文档](#docs-文档)
- [Themes 主题](#themes-主题)
- [License 许可证](#license-许可证)
- [Todo 后续功能](#todo-后续功能)
- [Thanks 感谢](#thanks-感谢)

## Introduction 简介

**Halo** [ˈheɪloʊ]，意为光环。当然，你也可以当成拼音读(哈喽)。

轻快，简洁，功能强大，使用Java开发的博客系统。

> **Halo交流群:** 162747721

## Quickstart 快速开始

```bash
git clone https://github.com/ruibaby/halo.git
cd halo
mvn clean package -Pprod
java -jar target/dist/halo/halo-xxx.jar
```

> **注意：如使用Idea，Eclipse等IDE运行的话，需要安装Lombok插件。**

Let's start: http://localhost:8090

## Demo 演示

[界面预览](https://halo-doc.ryanc.cc/preview)

[Ryan0up'S Blog](https://ryanc.cc)

[SNAIL BLOG](https://slogc.cc)

[宋浩志博客](http://songhaozhi.com)

## Download 下载部署

[下载](https://github.com/ruibaby/halo/releases)最新的压缩包，解压之后进入根目录执行`java -jar halo-xxx.jar`即可运行。

> **如需部署到服务器，请参考[Halo部署教程](https://ryanc.cc/archives/halo-run)。**

## Docs 文档

[Halo Document](https://halo-doc.ryanc.cc)

> **文档正在不断完善中。**

## Themes 主题

除了内置的[Anatole](https://github.com/hi-caicai/farbox-theme-Anatole)和[Material](https://github.com/viosey/hexo-theme-material)，还有下列主题没有集成在项目里，如有需要，请自行下载之后通过后台上传上去使用。

- [Vno](https://github.com/ruibaby/vno-halo) - 来自Jekyll的一款主题，作者[Wei Wang](https://onevcat.com/)。
- [Hux](https://github.com/ruibaby/hux-halo) - 来自Jekyll的一款主题，作者[Xuan Huang](https://huangxuan.me/)。

## License 许可证

[![license](https://img.shields.io/github/license/ruibaby/halo.svg)](https://github.com/ruibaby/halo/blob/master/LICENSE)

> **Halo使用GPL-v3.0协议开源，请尽量遵守开源协议，即便是在中国。**

## Todo 后续功能

- [ ] 文章阅读统计
- [ ] 文章顶置
- [ ] 集成又拍云，七牛云等云服务

## Thanks 感谢

Halo的诞生离不开下面这些项目：

- [IntelliJ IDEA](https://www.jetbrains.com/idea/)：个人认为最强大的Java IDE，没有之一
- [Spring Boot](https://github.com/spring-projects/spring-boot)：Spring的微服务框架
- [Freemarker](https://freemarker.apache.org/)：模板引擎，使页面静态化
- [H2 Database](https://github.com/h2database/h2database)：嵌入式数据库，无需安装
- [Druid](https://github.com/alibaba/druid)：阿里开发的连接池
- [Spring-data-jpa](https://github.com/spring-projects/spring-data-jpa.git)：不需要写sql语句的持久层框架
- [Ehcache](http://www.ehcache.org/)：缓存框架
- [Lombok](https://www.projectlombok.org/)：让代码更简洁
- [Apache Commons](http://commons.apache.org/)：非常好用的Java工具库
- [oh-my-email](https://github.com/biezhi/oh-my-email)：可能是最小的Java邮件发送库了，支持抄送、附件、模板等
- [AdminLTE](https://github.com/almasaeed2010/AdminLTE)：基于Bootstrap的后台模板
- [Bootstrap](https://github.com/twbs/bootstrap.git)：使用最广泛的前端ui框架
- [Animate](https://github.com/daneden/animate.css.git)：非常好用的css动效库
- [Editor.md](https://github.com/pandao/editor.md.git)：Markdown前端编辑器，遗憾作者弃坑了
- [Editor.md](https://github.com/hawtim/editor.md)：Editor.md，hawtim接过来维护的版本
- [Bootstrap-FileInput](https://github.com/kartik-v/bootstrap-fileinput.git)：个人认为最好用的上传组件，没有之一
- [Font-awesome](https://github.com/FortAwesome/Font-Awesome.git)：使用最广泛的字体图标库
- [Jquery](https://github.com/jquery/jquery.git)：使用最广泛的JavaScript框架
- [Layer](https://github.com/sentsin/layer.git)：个人认为最实用最好看的弹出层组件，没有之一
- [Jquery-Toast](https://github.com/kamranahmedse/jquery-toast-plugin)：消息提示组件
- [Pjax](https://github.com/defunkt/jquery-pjax.git)：pushState + ajax = pjax

