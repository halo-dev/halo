<#include "module/macro.ftl">
<@head title="${tag.tagName} · ${options.blog_title!'Anatole'}" keywords="${options.seo_keywords!'Anatole'}" description="${options.seo_desc!'Anatole'}"></@head>
<#include "module/sidebar.ftl">
<div class="main">
    <#include "module/page-top.ftl">
    <div class="autopagerize_page_element">
        <div class="content">
            <#include "module/post_entry.ftl">
            <#if posts.totalPages gt 1>
                <div class="pagination">
                    <ul class="clearfix">
                        <#if posts.hasPrevious()>
                            <#if posts.number == 1>
                                <li class="pre pagbuttons">
                                    <a class="btn" role="navigation" href="${options.blog_url!}/tags/${tag.tagUrl}">上一页</a>
                                </li>
                            <#else >
                                <li class="pre pagbuttons">
                                    <a class="btn" role="navigation" href="${options.blog_url!}/tags/${tag.tagUrl}/page/${posts.number}">上一页</a>
                                </li>
                            </#if>
                        </#if>
                        <#if posts.hasNext()>
                        <li class="next pagbuttons">
                            <a class="btn" role="navigation" href="${options.blog_url!}/tags/${tag.tagUrl}/page/${posts.number+2}">下一页</a>
                        </li>
                        </#if>
                    </ul>
                </div>
            </#if>
        </div>
    </div>
</div>
<@footer></@footer>
