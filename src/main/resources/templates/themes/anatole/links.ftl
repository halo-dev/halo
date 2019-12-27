<#include "module/macro.ftl">
<@head title="友情链接 · ${options.blog_title!}" keywords="${options.seo_keywords!}" description="${options.seo_description!}" />
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
                        <@linkTag method="list">
                            <#if links?? && links?size gt 0>
                                <#list links as link>
                                    <p>
                                        <a href="${link.url}" target="_blank" rel="external">${link.name}</a>
                                        <#if link.description!=''>
                                            – ${link.description}
                                        </#if>
                                    </p>
                                </#list>
                            </#if>
                        </@linkTag>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<@footer></@footer>
