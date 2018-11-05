<h1>
    <a href="#" target="_blank">Halo</a>
</h1>

> Halo, bir Java blog sistemidir.

[![JDK](https://img.shields.io/badge/JDK-1.8-yellow.svg)](#)
[![GitHub release](https://img.shields.io/github/release/ruibaby/halo.svg)](https://github.com/ruibaby/halo/releases)
[![Travis CI](https://img.shields.io/travis/ruibaby/halo.svg)](https://travis-ci.org/ruibaby/halo)
[![Docker Build Status](https://img.shields.io/docker/build/ruibaby/halo.svg)](https://hub.docker.com/r/ruibaby/halo/)

------------------------------
🇨🇳[简体中文](../README.md) | 🇺🇸[English](./README-en-US.md) | <img src="https://lipis.github.io/flag-icon-css/flags/4x3/tr.svg" alt="Turkish" height="14"/> Türkçe

<details><summary>Katalog</summary>

- [Giriş](#giris)
- [Hemen Başla](#hemen-başla)
- [Demo](#demo)
- [İndir](#İndir)
- [Dökümanlar](#dökümanlar)
- [Temalar](#temalar)
- [Lisans](#lisans)
- [Yapılacaklar Listesi](#yapılacaklar-listesi)
- [Teşekkür](#teşekkür)
- [Bağışlar](#bağışlar)

</details>

## Giriş

**Halo** [ˈheɪloʊ], Java kullanan en iyi blog sistemi olma yolunda.

Hızlı, öz ve güçlü bir blog sistemi.

> QQ Grup: 162747721，Telegram Grup：[https://t.me/HaloBlog](https://t.me/HaloBlog)

## Hemen Başla

```bash
git clone https://github.com/ruibaby/halo.git
cd halo
mvn clean package -Pprod
java -jar target/dist/halo/halo-latest.jar
```

Rapid server deploy etme:
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

> Not: Eğer çalıştırmak için Idea, Eclipse ve diğer IDE leri kullanırsanız, Lombok eklentisini yüklemeniz gerekir. Ayrıca JDK 10 şu anda desteklenmiyor ve temaların taranması ve yüklenmesi ile ilgili sorunlar var.


Hadi başlayalım: http://localhost:8090

## Demo

[Ryan0up'nın Bloğu](https://ryanc.cc)

[SNAIL'in Bloğu](https://slogc.cc)

[宋浩志博客](http://songhaozhi.com)

[KingYiFan'ın Bloğu](https://blog.cnbuilder.cn)

## İndir

> Sunucuya deploy etmek için gözat: [Halo部署教程](https://halo-doc.ryanc.cc/installation/) veya [Wiki](https://github.com/ruibaby/halo/wiki).

## Dökümanlar

[Halo Dökümanları](https://halo-doc.ryanc.cc)

> Dökümanlar sürekli geliştirilme aşamasındadır.

## Temalar

Ön bilgi olarak [Anatole](https://github.com/hi-caicai/farbox-theme-Anatole) ve [Material](https://github.com/viosey/hexo-theme-material) temları projeye entegre edilmedi. Eğer kullanmak isterseniz indirip arkaplanda yükleyebilirsiniz.
- [Vno](https://github.com/ruibaby/vno-halo) - Jekyll, Geliştirici [Wei Wang](https://onevcat.com/)。
- [Hux](https://github.com/ruibaby/hux-halo) - Jekyll, Geliştirici [Xuan Huang](https://huangxuan.me/)。
- [Story](https://github.com/ruibaby/story-halo) - Typecho, Geliştirici [Trii Hsia](https://yumoe.com/)。
- [NexT](https://github.com/ruibaby/next-halo) - Hexo, Geliştirici [iissnan](https://notes.iissnan.com/)。
- [Casper](https://github.com/ruibaby/casper-halo) - Ghost, Geliştirici [Ghost](https://github.com/TryGhost)。

> **Porting Theme**'nın işlevselliği ile ilgili yorum ve öneri kabul edilmemektedir.

## Lisans

[![license](https://img.shields.io/github/license/ruibaby/halo.svg)](https://github.com/ruibaby/halo/blob/master/LICENSE)

> Halo, açık kaynak kod olarak GPL-v3.0 kullanmaktadır.

## Yapılacaklar Listesi

- [x] Makale okunma istatistikleri
- [ ] Makale üstü
- [ ] Qiniu ve Upyun gibi bulut servisleri entegresi

## Teşekkür

Halo'nun ortaya çıkışı aşağıdaki projelerle olmuştur:

- [IntelliJ IDEA](https://www.jetbrains.com/idea/)：Kişisel görüş olarak en güçlü Java IDE si.
- [Spring Boot](https://github.com/spring-projects/spring-boot)：Spring'in microservis frameworkü.
- [Freemarker](https://freemarker.apache.org/)：Statik sayfa yapmak için şablon motoru.
- [H2 Database](https://github.com/h2database/h2database)：Gömülü veritabanı, kurulum gerekmez.
- [Druid](https://github.com/alibaba/druid): Alibaba tarafından geliştirilen veritabanı bağlantı havuzu.
- [Spring-data-jpa](https://github.com/spring-projects/spring-data-jpa.git)：Bir sql script katmanı yazmaya gerek yok.
- [Ehcache](http://www.ehcache.org/)：Cache sistemi.
- [Lombok](https://www.projectlombok.org/)：Kod basitleştirme.
- [oh-my-email](https://github.com/biezhi/oh-my-email)：Belkide en küçük java e-posta kütüphanesi. (CC, Ek, şablon desteği).
- [Hutool](https://github.com/looly/hutool)：Java tabanlı bir araç kütüphanesi.
- [Thumbnailator](https://github.com/coobird/thumbnailator)：Küçük resim oluşturma kütüphanesi.
- [AdminLTE](https://github.com/almasaeed2010/AdminLTE)：Bootstrap alt yapılı arka plan şablonu.
- [Bootstrap](https://github.com/twbs/bootstrap.git)：En kapsamlı front-end ui çatısı.
- [Animate](https://github.com/daneden/animate.css.git)：Kolay kullanımlı css kütüphanesi.
- [SimpleMDE - Markdown Editor](https://github.com/sparksuite/simplemde-markdown-editor)：Markdown editor.
- [Bootstrap-FileInput](https://github.com/kartik-v/bootstrap-fileinput.git)：Şahsen en iyi yükleme bileşeni olduğunu düşünyorum.
- [Font-awesome](https://github.com/FortAwesome/Font-Awesome.git)：En çok kullanılan font simgesi kütüphanesi.
- [Jquery](https://github.com/jquery/jquery.git)：Javascrip kullanımınızı genişletin.
- [Layer](https://github.com/sentsin/layer.git)：Kişisel görüşüm en pratik ve en iyi görünen pop-up katman bileşeni.
- [Jquery-Toast](https://github.com/kamranahmedse/jquery-toast-plugin)：Mesaj istemi bileşeni.
- [Pjax](https://github.com/defunkt/jquery-pjax.git)：pushState + ajax = pjax.****
- [OwO](https://github.com/DIYgod/OwO)：Front-end ifade kütüphanesi.

## Bağışlar

| Alipay  | Wechat  | Alipay Red envelope  |
| :------------: | :------------: | :------------: |
| <img src="https://cdn.ryanc.cc/img/github/donate/alipay.png" width="150"/>  | <img src="https://cdn.ryanc.cc/img/github/donate/wechat.png" width="150" />  | <img src="https://cdn.ryanc.cc/img/github/donate/hongbao.png" width="150" />  |


