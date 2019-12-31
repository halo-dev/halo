<#include "module/macro.ftl">
<@head title="归档 · ${options.blog_title!}" keywords="${options.seo_keywords!}" description="${options.seo_description!}" />
<#include "module/sidebar.ftl">
<div class="main">
    <#include "module/page-top.ftl">
    <div class="autopagerize_page_element">
        <div class="content">
            <div class="archive animated fadeInDown">
                <ul class="list-with-title">
                    <@postTag method="archiveYear">
                        <#list archives as archive>
                            <div class="listing-title">${archive.year?c}</div>
                            <ul class="listing">
                                <#list archive.posts?sort_by("createTime")?reverse as post>
                                    <div class="listing-item">
                                        <div class="listing-post">
                                            <a href="${context!}/archives/${post.url!}" title="${post.title!}">${post.title!}</a>
                                            <div class="post-time">
                                                <span class="date">${post.createTime?string("yyyy-MM-dd")}</span>
                                            </div>
                                        </div>
                                    </div>
                                </#list>
                            </ul>
                        </#list>
                    </@postTag>
                </ul>
            </div>
        </div>
    </div>
</div>
<@footer></@footer>
