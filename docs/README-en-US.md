<h1>
    <a href="#" target="_blank">Halo</a>
</h1>

> Halo may be the best Java blog system.

[![JDK](https://img.shields.io/badge/JDK-1.8-yellow.svg)](#)
[![GitHub release](https://img.shields.io/github/release/ruibaby/halo.svg)](https://github.com/ruibaby/halo/releases)
[![Travis CI](https://img.shields.io/travis/ruibaby/halo.svg)](https://travis-ci.org/ruibaby/halo)
[![Docker Build Status](https://img.shields.io/docker/build/ruibaby/halo.svg)](https://hub.docker.com/r/ruibaby/halo/)

------------------------------
🇨🇳[简体中文](../README.md) | 🇺🇸English | <img src="https://lipis.github.io/flag-icon-css/flags/4x3/tr.svg" alt="Turkish" height="14"/> [Türkçe](./README-tr.md)

<details><summary>Catalog</summary>

- [Introduction](#introduction)
- [Quickstart](#quickstart)
- [Demo](#demo)
- [Download](#download)
- [Docs](#docs)
- [Themes](#themes)
- [License](#license)
- [Todo](#todo)
- [Thanks](#thanks)
- [Donate](#donate)

</details>

## Introduction

**Halo** [ˈheɪloʊ],Become the best blogging system using Java.

Fast, concise, and powerful blogging system developed in Java.

> QQ Group: 162747721，Telegram Group：[https://t.me/HaloBlog](https://t.me/HaloBlog)

## Quickstart

```bash
git clone https://github.com/ruibaby/halo.git
cd halo
mvn clean package -Pprod
java -jar target/dist/halo/halo-latest.jar
```

Rapid server deployment:
```bash
# Install Halo
yum install -y wget && wget https://raw.githubusercontent.com/ruibaby/halo-cli/master/halo-cli.sh && bash halo-cli.sh -i
# Upgrade Halo
bash halo-cli.sh -u
```

Docker：
```bash
# Pull image
docker pull ruibaby/halo
# run
docker run -d --name halo -p 8090:8090 -v ~/halo:/root/halo ruibaby/halo
```

> Note: If you use Idea, Eclipse and other IDEs to run, you need to install the Lombok plugin, In addition, JDK10 is not supported at the moment, and there are problems with themes scanning and uploading.

Let's start: http://localhost:8090

## Demo

[Ryan0up'S Blog](https://ryanc.cc)

[SNAIL BLOG](https://slogc.cc)

[宋浩志博客](http://songhaozhi.com)

[KingYiFan'S Blog](https://blog.cnbuilder.cn)

## Download

> For deployment to the server, please refer to [Halo部署教程](https://halo-doc.ryanc.cc/installation/) or [Wiki](https://github.com/ruibaby/halo/wiki).

## Docs

[Halo Document](https://halo-doc.ryanc.cc)

> The documentation is constantly being improved.

## Themes

In addition to the built-in [Anatole](https://github.com/hi-caicai/farbox-theme-Anatole) and [Material](https://github.com/viosey/hexo-theme-material), there are the following The theme is not integrated in the project. If you need it, please download it and upload it through the background.
- [Vno](https://github.com/ruibaby/vno-halo) - From Jekyll,Author [Wei Wang](https://onevcat.com/)。
- [Hux](https://github.com/ruibaby/hux-halo) - From Jekyll,Author [Xuan Huang](https://huangxuan.me/)。
- [Story](https://github.com/ruibaby/story-halo) - From Typecho,Author [Trii Hsia](https://yumoe.com/)。
- [NexT](https://github.com/ruibaby/next-halo) - From Hexo,Author [iissnan](https://notes.iissnan.com/)。
- [Casper](https://github.com/ruibaby/casper-halo) - From Ghost,Author [Ghost](https://github.com/TryGhost)。

> Disclaimer: Do not accept any comments or suggestions on the functionality of the **Porting Theme**.

## License

[![license](https://img.shields.io/github/license/ruibaby/halo.svg)](https://github.com/ruibaby/halo/blob/master/LICENSE)

> Halo uses the GPL-v3.0 protocol to open source.

## Todo

- [x] Article reading statistics
- [ ] Article overhead
- [ ] Integrate cloud services such as Qiniu yun or Upyun

## Thanks

The birth of Halo is inseparable from the following projects:

- [IntelliJ IDEA](https://www.jetbrains.com/idea/)：Personally think that the most powerful Java IDE.
- [Spring Boot](https://github.com/spring-projects/spring-boot)：Spring's microservices framework.
- [Freemarker](https://freemarker.apache.org/)：Template engine to make the page static.
- [H2 Database](https://github.com/h2database/h2database)：Embedded database, no need to install.
- [Druid](https://github.com/alibaba/druid)：Database connection pool developed by Alibaba.
- [Spring-data-jpa](https://github.com/spring-projects/spring-data-jpa.git)：Do not need to write a sql script persistence layer framework.
- [Ehcache](http://www.ehcache.org/)：Cache framework.
- [Lombok](https://www.projectlombok.org/)：Make the code simpler.
- [oh-my-email](https://github.com/biezhi/oh-my-email)：Probably the smallest Java mailing library, support for CC, attachments, templates, etc.
- [Hutool](https://github.com/looly/hutool)：A Java base tool library.
- [Thumbnailator](https://github.com/coobird/thumbnailator)：Thumbnail generation library.
- [AdminLTE](https://github.com/almasaeed2010/AdminLTE)：Background template based on Bootstrap.
- [Bootstrap](https://github.com/twbs/bootstrap.git)：Use the most extensive front-end ui framework.
- [Animate](https://github.com/daneden/animate.css.git)：Very easy to use css motion library.
- [SimpleMDE - Markdown Editor](https://github.com/sparksuite/simplemde-markdown-editor)：Simple, functional, and lightweight Markdown editor.
- [Bootstrap-FileInput](https://github.com/kartik-v/bootstrap-fileinput.git)：Personally think that the best upload component.
- [Font-awesome](https://github.com/FortAwesome/Font-Awesome.git)：The most widely used font icon library.
- [Jquery](https://github.com/jquery/jquery.git)：Use the widest range of JavaScript frameworks.
- [Layer](https://github.com/sentsin/layer.git)：Personally think that the most practical and best-looking pop-up layer components.
- [Jquery-Toast](https://github.com/kamranahmedse/jquery-toast-plugin)：Message prompt component.
- [Pjax](https://github.com/defunkt/jquery-pjax.git)：pushState + ajax = pjax.****
- [OwO](https://github.com/DIYgod/OwO)：Front-end expression library.

## Donate

| Alipay  | Wechat  | Alipay Red envelope  |
| :------------: | :------------: | :------------: |
| <img src="https://cdn.ryanc.cc/img/github/donate/alipay.png" width="150"/>  | <img src="https://cdn.ryanc.cc/img/github/donate/wechat.png" width="150" />  | <img src="https://cdn.ryanc.cc/img/github/donate/hongbao.png" width="150" />  |


