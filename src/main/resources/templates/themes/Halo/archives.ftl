<#include "module/macro.ftl">
<@head title="归档 | ${options.site_title?if_exists}" keywords="${options.seo_keywords?if_exists}" description="${options.seo_desc?if_exists}"></@head>
<div class="wrapper" style="height: auto; min-height: 100%;">
<#include "module/header.ftl">
    <div class="main-content">
        <section class="container">
            <@articleTag method="archives">
                <#if archives ??>
                    <#list archives as archive>
                    <h1 class="animated fadeInDown">${archive.year}年${archive.month}月 (${archive.count})</h1>
                    <ul>
                        <#list archive.posts as post>
                            <li class="animated fadeInDown">
                                <a href="/archives/${post.postUrl}">${post.postTitle}</a>
                            </li>
                        </#list>
                    </ul>
                    </#list>
                </#if>
            </@articleTag>
        </section>
    </div>
<@footer></@footer>