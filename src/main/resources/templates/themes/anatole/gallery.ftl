<#include "/common/macro/common_macro.ftl">
<!DOCTYPE HTML>
<html>
<head>
    <title>图库 · ${options.blog_title!}</title>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <link rel="stylesheet" href="anatole/source/plugins/gallery/css/main.css" />
    <noscript><link rel="stylesheet" href="/source/plugins/gallery/css/noscript.css" /></noscript>
</head>
<body class="is-loading-0 is-loading-1 is-loading-2">
<div id="main">
    <header id="header">
        <h1>图库</h1>
        <p>${user.description!}</p>
        <ul class="icons">
            <!--
            <li><a href="#" class="icon fa-twitter"><span class="label">Twitter</span></a></li>
            <li><a href="#" class="icon fa-instagram"><span class="label">Instagram</span></a></li>
            <li><a href="#" class="icon fa-github"><span class="label">Github</span></a></li>
            <li><a href="#" class="icon fa-envelope-o"><span class="label">Email</span></a></li>
            -->
        </ul>
    </header>
    <section id="thumbnails">
        <@galleryTag method="list">
            <#if galleries?size gt 0>
                <#list galleries as gallery>
                    <article>
                        <a class="thumbnail" href="${gallery.url}" data-position="left center"><img src="${gallery.thumbnail}" alt="${gallery.description}" /></a>
                        <h2>${gallery.name}</h2>
                        <p>${gallery.takeTime!}</p>
                    </article>
                </#list>
            </#if>
        </@galleryTag>
    </section>
    <footer id="footer">
        <ul class="copyright">
            <li>&copy; ${options.blog_title!}.</li><li>Design: <a href="http://html5up.net">HTML5 UP</a>.</li>
        </ul>
    </footer>
</div>
<script src="anatole/source/plugins/gallery/js/jquery.min.js"></script>
<script src="anatole/source/plugins/gallery/js/skel.min.js"></script>
<script src="anatole/source/plugins/gallery/js/main.js"></script>
<@statistics></@statistics>
</body>
</html>
