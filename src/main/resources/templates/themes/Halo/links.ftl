<#include "module/macro.ftl">
<@head title="友情链接 | ${options.site_title?if_exists}" keywords="${options.seo_keywords?if_exists}" description="${options.seo_desc?if_exists}"></@head>
<div class="wrapper" style="height: auto; min-height: 100%;">
<#include "module/header.ftl">
    <div class="main-content">
        <section class="container">
            <div class="row link-list">
                <div class="col-lg-4 link-list-item" data-aos="fade-up">
                    <div class="link-item-main">
                        <a href="#">
                            <div class="link-item-avatar">
                                <img src="https://cdn.ryanc.cc/img/blog/site/avatar.png" height="96px">
                            </div>
                            <div class="link-list-info" style="display: inline-block;">

                            </div>
                        </a>
                    </div>
                </div>
                <div class="col-lg-4 link-list-item" data-aos="fade-up">
                    <div class="link-item-main">
                        <a href="#">
                            <img src="/halo/source/img/pic1.jpg" height="96px">
                        </a>
                    </div>
                </div>
                <div class="col-lg-4 link-list-item" data-aos="fade-up">
                    <div class="link-item-main">
                        <a href="#">
                            <img src="/halo/source/img/pic13.jpg" height="96px">
                        </a>
                    </div>
                </div>
                <div class="col-lg-4 link-list-item" data-aos="fade-up">
                    <div class="link-item-main">
                        <a href="#">
                            <img src="/halo/source/img/pic14.jpg" height="96px">
                        </a>
                    </div>
                </div>
                <div class="col-lg-4 link-list-item" data-aos="fade-up">
                    <div class="link-item-main">
                        <a href="#">
                            <img src="/halo/source/img/pic19.jpg" height="96px">
                        </a>
                    </div>
                </div>
                <div class="col-lg-4 link-list-item" data-aos="fade-up">
                    <div class="link-item-main">
                        <a href="#">
                            <img src="/halo/source/img/pic11.jpg" height="96px">
                        </a>
                    </div>
                </div>
            </div>
            <#include "module/comment.ftl">
        </section>
    </div>
<@footer></@footer>