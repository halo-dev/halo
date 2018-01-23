<#include "module/marco.ftl">
<@head title="友情链接 · ${options.site_title?default('Anatole')}" keywords="${options.seo_keywords?default('Anatole')}" description="${options.seo_desc?default('Anatole')}"></@head>
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
                        <#list links as link>
                        <p><a href="${link.linkUrl}" target="_blank" rel="external">${link.linkName}</a> – ${link.linkDesc}</p>
                        </#list>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<@footer></@footer>