<#include "module/marco.ftl">
<@head title="归档 · ${options.site_title?default('Anatole')}" keywords="文章归档,${options.seo_keywords?default('Anatole')}" description="${options.seo_desc?default('Anatole')}"></@head>
<#include "module/sidebar.ftl">
<div class="main">
    <#include "module/page-top.ftl">
    <div class="autopagerize_page_element">
        <div class="content">
            <div class="archive animated fadeInDown">
                <ul class="list-with-title">
                    <#list archives as archive>
                        <div class="listing-title">${archive.year}.${archive.month}</div>
                        <ul class="listing">
                            <#list archive.posts as post>
                                <div class="listing-item">
                                    <div class="listing-post">
                                        <a href="/post/${post.postUrl}" title="${post.postTitle}">${post.postTitle}</a>
                                        <div class="post-time">
                                            <span class="date">${post.postDate?string("yyyy-MM-dd")}</span>
                                        </div>
                                    </div>
                                </div>
                            </#list>
                        </ul>
                    </#list>
                </ul>
            </div>
        </div>
    </div>
</div>
<@footer></@footer>