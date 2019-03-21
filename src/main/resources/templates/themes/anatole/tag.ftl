<#include "module/macro.ftl">
<@head title="标签：${tag.name} · ${options.blog_title!'Halo'}" keywords="${options.seo_keywords!'Halo'}" description="${options.seo_desc!'Halo'}"></@head>
<#include "module/sidebar.ftl">
<div class="main">
    <#include "module/page-top.ftl">
    <div class="autopagerize_page_element">
        <div class="content">
            <#include "module/post_entry.ftl">
            <#if posts.pages gt 1>
                <div class="pagination">
                    <ul class="clearfix">
                        <#if posts.hasPrevious()>
                            <#if posts.number == 1>
                                <li class="pre pagbuttons">
                                    <a class="btn" role="navigation" href="${options.blog_url!}/tags/${tag.slugName}">上一页</a>
                                </li>
                            <#else >
                                <li class="pre pagbuttons">
                                    <a class="btn" role="navigation" href="${options.blog_url!}/tags/${tag.slugName}/page/${posts.page}">上一页</a>
                                </li>
                            </#if>
                        </#if>
                        <#if posts.hasNext()>
                        <li class="next pagbuttons">
                            <a class="btn" role="navigation" href="${options.blog_url!}/tags/${tag.slugName}/page/${posts.page+2}">下一页</a>
                        </li>
                        </#if>
                    </ul>
                </div>
            </#if>
        </div>
    </div>
</div>
<@footer></@footer>
