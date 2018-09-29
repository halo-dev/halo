<#include "module/macro.ftl">
<@head title="标签 · ${options.blog_title?default('Anatole')}" keywords="${options.seo_keywords?default('Anatole')}" description="${options.seo_desc?default('Anatole')}"></@head>
<#include "module/sidebar.ftl">
<div class="main">
<#include "module/page-top.ftl">
    <div class="autopagerize_page_element">
        <div class="content">
            <div class="tags-cloud animated fadeInDown">
            <@commonTag method="tags">
                <#if tags?? && tags?size gt 0>
                    <#list tags as tag>
                        <a href="/tags/${tag.tagUrl}/">${tag.tagName}</a>
                    </#list>
                </#if>
            </@commonTag>
            </div>
        </div>
    </div>
</div>
<@footer></@footer>
