<#include "module/macro.ftl">
<@head title="友情链接 · ${options.blog_title!'Anatole'}" keywords="${options.seo_keywords!'Anatole'}" description="${options.seo_desc!'Anatole'}"></@head>
<#include "module/sidebar.ftl">
<div class="main">
    <#include "module/page-top.ftl">
    <div class="autopagerize_page_element">
        <div class="content">
            <div class="post-page">
                <div class="post animated fadeInDown">
                    <div class="post-title">
                        <h3><a>links</a></h3>
                    </div>
                    <div class="post-content">
                        <@commonTag method="links">
                            <#if links?? && links?size gt 0>
                                <#list links as link>
                                <p>
                                    <a href="${link.linkUrl}" target="_blank" rel="external">${link.linkName}</a>
                                    <#if link.linkDesc!=''>
                                         – ${link.linkDesc}
                                    </#if>
                                </p>
                                </#list>
                            </#if>
                        </@commonTag>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<@footer></@footer>
