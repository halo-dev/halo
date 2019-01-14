![](https://i.loli.net/2018/12/21/5c1cd34849751.png)

> Halo 可能是最好的 Java 博客系统。

<p align="center">
<a href="https://ryanc.cc"><img alt="Author" src="https://img.shields.io/badge/author-ruibaby-red.svg?style=flat-square"/></a>
<a href="#"><img alt="JDK" src="https://img.shields.io/badge/JDK-1.8-yellow.svg?style=flat-square"/></a>
<a href="https://github.com/ruibaby/halo/releases"><img alt="GitHub release" src="https://img.shields.io/github/release/ruibaby/halo.svg?style=flat-square"/></a>
<a href="https://travis-ci.org/ruibaby/halo"><img alt="Travis CI" src="https://img.shields.io/travis/ruibaby/halo.svg?style=flat-square"/></a>
<a href="https://hub.docker.com/r/ruibaby/halo/"><img alt="Docker Build Status" src="https://img.shields.io/docker/build/ruibaby/halo.svg?style=flat-square"/></a>
</p>

------------------------------
🇨🇳简体中文 | 🇺🇸[English](README-en_US.md)

## 简介

**Halo** [ˈheɪloʊ]，意为光环。当然，你也可以当成拼音读(哈喽)。

轻快，简洁，功能强大，使用 Java 开发的博客系统。

> QQ 交流群: 162747721 | Telegram 交流群：[https://t.me/HaloBlog](https://t.me/HaloBlog) | Telegram 频道：[https://t.me/halo_dev](https://t.me/halo_dev) | [WeHalo 小程序](https://github.com/aquanlerou/WeHalo)。

## 演示站点

> 前台地址：[https://demo.halo.run](https://demo.halo.run) 
> 后台地址：[https://demo.halo.run/admin](https://demo.halo.run/admin)
> 用户名：admin，密码：123456

## 快速开始

服务器快速部署：

```bash
# 安装 Halo
yum install -y wget && wget -O halo-cli.sh https://git.io/fxHqp && bash halo-cli.sh -i

# 更新 Halo
bash halo-cli.sh -u
```

Docker 部署：
```bash
# 拉取镜像
docker pull ruibaby/halo

# 运行
docker run -d --name halo -p 8090:8090 -v ~/halo:/root/halo ruibaby/halo
```

Docker Compose 部署：
```bash
# 下载 Nginx 配置文件模板
curl https://raw.githubusercontent.com/jwilder/nginx-proxy/master/nginx.tmpl > /etc/nginx/nginx.tmpl

# 获取 docker-compose.yaml 文件
yum install -y wget && wget -O docker-compose.yaml https://git.io/fpS8N

# 修改 docker-compose.yaml
# 1. 修改 VIRTUAL_HOST,LETSENCRYPT_HOST 为自己的域名。
# 2. 修改 LETSENCRYPT_EMAIL 为自己的邮箱。
# 3. 修改 DB_USER 数据库用户名，注意：这是自定义的，请不要使用默认的！下面数据库密码同理。
# 4. 修改 DB_PASSWORD 数据库密码。

# 运行
docker-compose up -d
```

> 注意：如使用 Idea，Eclipse 等IDE运行的话，需要安装Lombok插件，另外暂不支持JDK10，主题管理和主题上传会有问题。
> 更多请参考[ Halo 使用文档 ](https://halo-doc.ryanc.cc/installation/)或者[ Wiki](https://github.com/ruibaby/halo/wiki)。

## 博客示例

[Ryan0up'S Blog](https://ryanc.cc)

[SNAIL BLOG](https://slogc.cc)

[宋浩志博客](http://songhaozhi.com)

[KingYiFan'S Blog](https://blog.cnbuilder.cn)

[AquanBlog](https://blog.eunji.cn/)

## 主题

除了内置的 [Anatole](https://github.com/hi-caicai/farbox-theme-Anatole) 和 [Material](https://github.com/viosey/hexo-theme-material) ，还有下列主题没有集成在项目里，如有需要，请自行下载之后通过后台上传上去使用。

- [Vno](https://github.com/ruibaby/vno-halo) - 来自 Jekyll 的一款主题，作者 [Wei Wang](https://onevcat.com/)。
- [Hux](https://github.com/ruibaby/hux-halo) - 来自 Jekyll 的一款主题，作者 [Xuan Huang](https://huangxuan.me/)。
- [Story](https://github.com/ruibaby/story-halo) - 来自 Typecho 的一款主题，作者 [Trii Hsia](https://yumoe.com/)。
- [NexT](https://github.com/ruibaby/next-halo) - 来自 Hexo 的一款主题，作者 [iissnan](https://notes.iissnan.com/)。
- [Casper](https://github.com/ruibaby/casper-halo) - 来自 Ghost 的一款主题，作者 [Ghost](https://github.com/TryGhost)。
- [Pinghsu](https://github.com/ruibaby/pinghsu-halo) - 来自 Typecho 的一款主题，作者 [Chakhsu.Lau](https://github.com/chakhsu)。

> 声明：不接受任何对**移植主题**功能上的意见和建议。

## 许可证

[![license](https://img.shields.io/github/license/ruibaby/halo.svg?style=flat-square)](https://github.com/ruibaby/halo/blob/master/LICENSE)

> Halo 使用 GPL-v3.0 协议开源，请尽量遵守开源协议，即便是在中国。

## 感谢

Halo 的诞生离不开下面这些项目：

- [Spring Boot](https://github.com/spring-projects/spring-boot)：Spring 的快速开发框架
- [Freemarker](https://freemarker.apache.org/)：模板引擎，使页面静态化
- [H2 Database](https://github.com/h2database/h2database)：嵌入式数据库，无需安装
- [Spring-data-jpa](https://github.com/spring-projects/spring-data-jpa.git)：不需要写 sql 语句的持久层框架
- [Ehcache](http://www.ehcache.org/)：缓存框架
- [Lombok](https://www.projectlombok.org/)：让代码更简洁
- [oh-my-email](https://github.com/biezhi/oh-my-email)：可能是最小的 Java 邮件发送库了，支持抄送、附件、模板等
- [Hutool](https://github.com/looly/hutool)：一个 Java 基础工具类库
- [Thumbnailator](https://github.com/coobird/thumbnailator)：缩略图生成库
- [AdminLTE](https://github.com/almasaeed2010/AdminLTE)：基于 Bootstrap 的后台模板
- [Bootstrap](https://github.com/twbs/bootstrap.git)：使用最广泛的前端 ui 框架
- [Animate](https://github.com/daneden/animate.css.git)：非常好用的 css 动效库
- [SimpleMDE - Markdown Editor](https://github.com/sparksuite/simplemde-markdown-editor)：简洁，功能够用，且轻量级的 Markdown 编辑器
- [Bootstrap-FileInput](https://github.com/kartik-v/bootstrap-fileinput.git)：基于 Bootstrap 的文件上传组件
- [Font-awesome](https://github.com/FortAwesome/Font-Awesome.git)：使用最广泛的字体图标库
- [JQuery](https://github.com/jquery/jquery.git)：使用最广泛的 JavaScript 框架
- [Layer](https://github.com/sentsin/layer.git)：个人认为最实用最好看的弹出层组件，没有之一
- [JQuery-Toast](https://github.com/kamranahmedse/jquery-toast-plugin)：消息提示组件
- [Pjax](https://github.com/defunkt/jquery-pjax.git)：pushState + ajax = pjax
- [OwO](https://github.com/DIYgod/OwO)：前端表情库

## 捐赠

> 如果 Halo 对你有帮助，可以请作者喝杯☕️。

| 支付宝/微信/QQ  |
| :------------: |
| <img src="https://i.loli.net/2018/12/23/5c1f68ce9b884.png" width="200"/>  |

## 界面展示

![](https://i.loli.net/2018/12/16/5c15b6edb9a49.png)
![](https://i.loli.net/2018/12/16/5c15b6ee08333.png)
![](https://i.loli.net/2018/12/16/5c15b6ec853af.png)
![](https://i.loli.net/2018/12/16/5c15b6ec50238.png)
![](https://i.loli.net/2018/12/16/5c15b6ed4057a.png)
![](https://i.loli.net/2018/12/16/5c15b6eb01f2d.png)
![](https://i.loli.net/2018/12/16/5c15b6eb98898.png)
![](https://i.loli.net/2018/12/16/5c15b6eb3b506.png)
![](https://i.loli.net/2018/12/16/5c15b6ebf29fd.png)
