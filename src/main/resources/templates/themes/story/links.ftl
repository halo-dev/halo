<#include "header.ftl">
<@header title="友情链接 - ${options.blog_title?default('Story')}" desc="${options.seo_desc?default('Story')}" keywords="${options.seo_keywords?default('Story')}"></@header>
<div class="container-fluid">
    <div class="row">
        <div id="main" class="col-12 clearfix" role="main">
            <article class="posti" itemscope itemtype="http://schema.org/BlogPosting">
                <h3>Links</h3>
                <div class="post-tags">
                    <ul>
                    <@commonTag method="links">
                        <#if links?size gt 0>
                            <#list links as link>
                                <li rel="tag"><a target="_blank" href="${link.linkUrl}">${link.linkName}</a></li>
                            </#list>
                        </#if>
                    </@commonTag>
                    </ul>
                </div>
            </article>
        </div>
    </div>
</div>
<#include "footer.ftl">
