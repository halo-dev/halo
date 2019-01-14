![](https://i.loli.net/2018/12/21/5c1cd34849751.png)

> Halo may be the best Java blog system.

<p align="center">
<a href="https://ryanc.cc"><img alt="Author" src="https://img.shields.io/badge/author-ruibaby-red.svg?style=flat-square"/></a>
<a href="#"><img alt="JDK" src="https://img.shields.io/badge/JDK-1.8-yellow.svg?style=flat-square"/></a>
<a href="https://github.com/ruibaby/halo/releases"><img alt="GitHub release" src="https://img.shields.io/github/release/ruibaby/halo.svg?style=flat-square"/></a>
<a href="https://travis-ci.org/ruibaby/halo"><img alt="Travis CI" src="https://img.shields.io/travis/ruibaby/halo.svg?style=flat-square"/></a>
<a href="https://hub.docker.com/r/ruibaby/halo/"><img alt="Docker Build Status" src="https://img.shields.io/docker/build/ruibaby/halo.svg?style=flat-square"/></a>
</p>

------------------------------
🇨🇳[简体中文](README.md) | 🇺🇸English

## Introduction

**Halo** [ˈheɪloʊ], Become the best blogging system using Java.

Fast, concise, and powerful blogging system developed in Java.

> QQ Group: 162747721，Telegram Group: [https://t.me/HaloBlog](https://t.me/HaloBlog) | Telegram Channel: [https://t.me/halo_dev](https://t.me/halo_dev) | [WeHalo 小程序](https://github.com/aquanlerou/WeHalo)。

## Demo

> Frontend: [https://demo.halo.run](https://demo.halo.run) 
> Backend: [https://demo.halo.run/admin](https://demo.halo.run/admin)
> username: admin,password: 123456

## Quick start

Deploy with shell script：

```bash
# install Halo
yum install -y wget && wget -O halo-cli.sh https://git.io/fxHqp && bash halo-cli.sh -i

# upgrade Halo
bash halo-cli.sh -u
```

Deploy with Docker：
```bash
# pull docker images
docker pull ruibaby/halo

# create docker container and run it
docker run -d --name halo -p 8090:8090 -v ~/halo:/root/halo ruibaby/halo
```

Deploy with Docker compose：
```bash
# Download the nginx config file template
curl https://raw.githubusercontent.com/jwilder/nginx-proxy/master/nginx.tmpl > /etc/nginx/nginx.tmpl

# Get the docker-compose.yaml
yum install -y wget && wget -O docker-compose.yaml https://git.io/fpS8N

# Modify docker-compose.yaml
# 1. modify VIRTUAL_HOST, LETSENCRYPT_HOST for your own domain name.
# 2. modify LETSENCRYPT_EMAIL to your own mailbox.
# 3. modify DB_USER .
# 4. modify DB_PASSWORD .

# run
docker-compose up -d
```

> Tips: If you use Idea, Eclipse and other IDEs to run, you need to install the Lombok plugin, In addition, JDK10 is not supported at the moment, and there are problems with themes scanning and uploading.
> See the [Halo documentation](https://halo-doc.ryanc.cc/installation/) or [ Wiki](https://github.com/ruibaby/halo/wiki) for more details.

## Blogs with Halo

[Ryan0up'S Blog](https://ryanc.cc)

[SNAIL BLOG](https://slogc.cc)

[宋浩志博客](http://songhaozhi.com)

[KingYiFan'S Blog](https://blog.cnbuilder.cn)

[AquanBlog](https://blog.eunji.cn/)

## Themes

In addition to the built-in [Anatole](https://github.com/hi-caicai/farbox-theme-Anatole) and [Material](https://github.com/viosey/hexo-theme-material), there are the following The theme is not integrated in the project. If you need it, please download it and upload it through the background.

- [Vno](https://github.com/ruibaby/vno-halo) - From Jekyll,Author [Wei Wang](https://onevcat.com/).
- [Hux](https://github.com/ruibaby/hux-halo) - From Jekyll,Author [Xuan Huang](https://huangxuan.me/).
- [Story](https://github.com/ruibaby/story-halo) - From Typecho,Author [Trii Hsia](https://yumoe.com/).
- [NexT](https://github.com/ruibaby/next-halo) - From Hexo,Author [iissnan](https://notes.iissnan.com/).
- [Casper](https://github.com/ruibaby/casper-halo) - From Ghost,Author [Ghost](https://github.com/TryGhost).
- [Pinghsu](https://github.com/ruibaby/pinghsu-halo) - From Typecho,Author [Chakhsu.Lau](https://github.com/chakhsu).

> Disclaimer: Do not accept any comments or suggestions on the functionality of the **Porting Theme**.

## License

[![license](https://img.shields.io/github/license/ruibaby/halo.svg?style=flat-square)](https://github.com/ruibaby/halo/blob/master/LICENSE)

> Halo uses the GPL-v3.0 protocol to open source.

## Thanks

The birth of Halo is inseparable from the following projects：

- [Spring Boot](https://github.com/spring-projects/spring-boot): Spring's rapid development framework
- [Freemarker](https://freemarker.apache.org/): Template engine to make pages static
- [H2 Database](https://github.com/h2database/h2database): Embedded database, no need to install
- [Spring-data-jpa](https://github.com/spring-projects/spring-data-jpa.git): a persistence layer framework that does not require writing sql statements
- [Ehcache](http://www.ehcache.org/): Cache Framework
- [Lombok](https://www.projectlombok.org/): Make the code simpler
- [oh-my-email](https://github.com/biezhi/oh-my-email): Probably the smallest Java mailing library, support for CC, attachments, templates, etc.
- [Hutool](https://github.com/looly/hutool): A Java Foundation Tools library
- [Thumbnailator](https://github.com/coobird/thumbnailator): thumbnail generation library
- [AdminLTE](https://github.com/almasaeed2010/AdminLTE): Bootstrap-based background template
- [Bootstrap](https://github.com/twbs/bootstrap.git): The most widely used front-end ui framework
- [Animate](https://github.com/daneden/animate.css.git): Very easy to use css effects library
- [SimpleMDE - Markdown Editor](https://github.com/sparksuite/simplemde-markdown-editor): Simple, functional, and lightweight Markdown editor
- [Bootstrap-FileInput](https://github.com/kartik-v/bootstrap-fileinput.git): Bootstrap-based file upload component
- [Font-awesome](https://github.com/FortAwesome/Font-Awesome.git): the most widely used font icon library
- [JQuery](https://github.com/jquery/jquery.git): The most widely used JavaScript framework
- [Layer](https://github.com/sentsin/layer.git): Personally think that the most practical and best-looking pop-up layer component, no one
- [JQuery-Toast](https://github.com/kamranahmedse/jquery-toast-plugin)
: message prompt component
- [Pjax](https://github.com/defunkt/jquery-pjax.git): pushState + ajax = pjax
- [OwO](https://github.com/DIYgod/OwO): front-end expression library

## Donate

> If Halo is helpful to you, ask the author to have a ☕.

| AliPay/WeChat/QQ/PayPal  |
| :------------: |
| <img src="https://i.loli.net/2018/12/23/5c1f68ce9b884.png" width="200"/>  |

## Interface display

![](https://i.loli.net/2018/12/16/5c15b6edb9a49.png)
![](https://i.loli.net/2018/12/16/5c15b6ee08333.png)
![](https://i.loli.net/2018/12/16/5c15b6ec853af.png)
![](https://i.loli.net/2018/12/16/5c15b6ec50238.png)
![](https://i.loli.net/2018/12/16/5c15b6ed4057a.png)
![](https://i.loli.net/2018/12/16/5c15b6eb01f2d.png)
![](https://i.loli.net/2018/12/16/5c15b6eb98898.png)
![](https://i.loli.net/2018/12/16/5c15b6eb3b506.png)
![](https://i.loli.net/2018/12/16/5c15b6ebf29fd.png)
