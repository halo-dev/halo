<h1>
    <a href="#" target="_blank">Halo</a>
</h1>

> Halo可能是最好的Java博客系统。

[![JDK](https://img.shields.io/badge/JDK-1.8-yellow.svg)](#)
[![GitHub release](https://img.shields.io/github/release/ruibaby/halo.svg)](https://github.com/ruibaby/halo/releases)
[![Travis CI](https://img.shields.io/travis/ruibaby/halo.svg)](https://travis-ci.org/ruibaby/halo)
[![Docker Build Status](https://img.shields.io/docker/build/ruibaby/halo.svg)](https://hub.docker.com/r/ruibaby/halo/)

------------------------------
🇨🇳简体中文 | 🇺🇸[English](./docs/README-en-US.md) | <img src="https://lipis.github.io/flag-icon-css/flags/4x3/tr.svg" alt="Turkish" height="14"/> [Türkçe](./docs/README-tr.md)

<details><summary>目录</summary>

- [简介](#简介)
- [快速开始](#快速开始)
- [演示](#演示)
- [下载部署](#下载部署)
- [文档](#文档)
- [主题](#主题)
- [许可证](#许可证)
- [后续功能](#后续功能)
- [感谢](#感谢)
- [捐赠](#捐赠)

</details>

## 简介

**Halo** [ˈheɪloʊ]，意为光环。当然，你也可以当成拼音读(哈喽)。

轻快，简洁，功能强大，使用Java开发的博客系统。

> QQ交流群: 162747721，Telegram交流群：[https://t.me/HaloBlog](https://t.me/HaloBlog)

## 快速开始

```bash
git clone https://github.com/ruibaby/halo.git
cd halo
mvn clean package -Pprod
java -jar target/dist/halo/halo-latest.jar
```

服务器快速部署：

```bash
# 安装Halo
yum install -y wget && wget https://raw.githubusercontent.com/ruibaby/halo-cli/master/halo-cli.sh && bash halo-cli.sh -i
# 更新Halo
bash halo-cli.sh -u
```

Docker 部署：
```bash
# 拉取镜像
docker pull ruibaby/halo

# 运行
docker run -d --name halo -p 8090:8090 -v ~/halo:/root/halo ruibaby/halo
```

> 注意：如使用Idea，Eclipse等IDE运行的话，需要安装Lombok插件，另外暂不支持JDK10，主题扫描和上传会有问题。

Let's start: http://localhost:8090

## 演示

[Ryan0up'S Blog](https://ryanc.cc)

[SNAIL BLOG](https://slogc.cc)

[宋浩志博客](http://songhaozhi.com)

[KingYiFan'S Blog](https://blog.cnbuilder.cn)

## 下载部署

> 如需部署到服务器，请参考[Halo部署教程](https://halo-doc.ryanc.cc/installation/)或者[Wiki](https://github.com/ruibaby/halo/wiki)。

## 文档

[Halo Document](https://halo-doc.ryanc.cc)

> 文档正在不断完善中。

## 主题

除了内置的[Anatole](https://github.com/hi-caicai/farbox-theme-Anatole)和[Material](https://github.com/viosey/hexo-theme-material)，还有下列主题没有集成在项目里，如有需要，请自行下载之后通过后台上传上去使用。

- [Vno](https://github.com/ruibaby/vno-halo) - 来自Jekyll的一款主题，作者[Wei Wang](https://onevcat.com/)。
- [Hux](https://github.com/ruibaby/hux-halo) - 来自Jekyll的一款主题，作者[Xuan Huang](https://huangxuan.me/)。
- [Story](https://github.com/ruibaby/story-halo) - 来自Typecho的一款主题，作者[Trii Hsia](https://yumoe.com/)。
- [NexT](https://github.com/ruibaby/next-halo) - 来自Hexo的一款主题，作者[iissnan](https://notes.iissnan.com/)。
- [Casper](https://github.com/ruibaby/casper-halo) - 来自Ghost的一款主题，作者[Ghost](https://github.com/TryGhost)。

> 声明：不接受任何对**移植主题**功能上的意见和建议。

## 许可证

[![license](https://img.shields.io/github/license/ruibaby/halo.svg)](https://github.com/ruibaby/halo/blob/master/LICENSE)

> Halo使用GPL-v3.0协议开源，请尽量遵守开源协议，即便是在中国。

## 后续功能

- [x] 文章阅读统计
- [ ] 文章顶置
- [ ] 集成又拍云，七牛云等云服务

## 感谢

Halo的诞生离不开下面这些项目：

- [IntelliJ IDEA](https://www.jetbrains.com/idea/)：个人认为最强大的Java IDE，没有之一
- [Spring Boot](https://github.com/spring-projects/spring-boot)：Spring的微服务框架
- [Freemarker](https://freemarker.apache.org/)：模板引擎，使页面静态化
- [H2 Database](https://github.com/h2database/h2database)：嵌入式数据库，无需安装
- [Druid](https://github.com/alibaba/druid)：阿里开发的连接池
- [Spring-data-jpa](https://github.com/spring-projects/spring-data-jpa.git)：不需要写sql语句的持久层框架
- [Ehcache](http://www.ehcache.org/)：缓存框架
- [Lombok](https://www.projectlombok.org/)：让代码更简洁
- [oh-my-email](https://github.com/biezhi/oh-my-email)：可能是最小的Java邮件发送库了，支持抄送、附件、模板等
- [Hutool](https://github.com/looly/hutool)：一个Java基础工具类库
- [Thumbnailator](https://github.com/coobird/thumbnailator)：缩略图生成库
- [AdminLTE](https://github.com/almasaeed2010/AdminLTE)：基于Bootstrap的后台模板
- [Bootstrap](https://github.com/twbs/bootstrap.git)：使用最广泛的前端ui框架
- [Animate](https://github.com/daneden/animate.css.git)：非常好用的css动效库
- [SimpleMDE - Markdown Editor](https://github.com/sparksuite/simplemde-markdown-editor)：简洁，功能够用，且轻量级的Markdown编辑器
- [Bootstrap-FileInput](https://github.com/kartik-v/bootstrap-fileinput.git)：个人认为最好用的上传组件，没有之一
- [Font-awesome](https://github.com/FortAwesome/Font-Awesome.git)：使用最广泛的字体图标库
- [Jquery](https://github.com/jquery/jquery.git)：使用最广泛的JavaScript框架
- [Layer](https://github.com/sentsin/layer.git)：个人认为最实用最好看的弹出层组件，没有之一
- [Jquery-Toast](https://github.com/kamranahmedse/jquery-toast-plugin)：消息提示组件
- [Pjax](https://github.com/defunkt/jquery-pjax.git)：pushState + ajax = pjax
- [OwO](https://github.com/DIYgod/OwO)：前端表情库

## 捐赠

> 如果Halo对你有帮助，可以请作者喝瓶娃哈哈哈哈哈哈哈哈哈哈。

| 支付宝  | 微信  | 支付宝红包  |
| :------------: | :------------: | :------------: |
| <img src="https://cdn.ryanc.cc/img/github/donate/alipay.png" width="150"/>  | <img src="https://cdn.ryanc.cc/img/github/donate/wechat.png" width="150" />  | <img src="https://cdn.ryanc.cc/img/github/donate/hongbao.png" width="150" />  |


