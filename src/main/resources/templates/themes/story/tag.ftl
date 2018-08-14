<#include "header.ftl">
<@header title="${tag.tagName} - ${options.blog_title?default('Story')}" desc="${options.seo_desc?default('Story')}" keywords="${options.seo_keywords?default('Story')}"></@header>
<div class="container-fluid">
    <div class="row">
        <div class="archive-header">
            <span>- Tag · ${tag.tagName} -</span>
        </div>
    </div>
</div>

<div class="container-fluid">
    <div class="row">
        <div id="main" role="main">
            <ul class="post-list clearfix">
                <#if posts.content?size gt 0>
                <#list posts.content as post>
                <li class="post-item grid-item" itemscope itemtype="http://schema.org/BlogPosting">
                    <a class="post-link" href="/archives/${post.postUrl}">
                        <h3 class="post-title"><time class="index-time" datetime="${post.postDate}" itemprop="datePublished">${post.postDate?string('MMM d,yyyy')}</time><br>${post.postTitle}</h3>
                        <div class="post-meta">
                            <#if post.categories?? && post.categories?size gt 0>
                                ${post.categories[0].cateName}
                            </#if>
                        </div>
                    </a>
                </li>
                </#list>
                <#else >
                <br><br>
                <h2 class="post-title"><center>(´°̥̥̥̥̥̥̥̥ω°̥̥̥̥̥̥̥̥｀) 什么都没有找到唉...</center></h2>
                </#if>
            </ul>
        </div>
    </div>
</div>

<div class="container-fluid">
    <div class="row">
        <#if posts.totalPages gt 1>
            <div class="nav-page">
                <ol class="page-navigator">
                    <#if posts.hasPrevious()>
                        <#if posts.number == 1>
                        <li class="prev">
                            <a href="/tags/${tag.tagUrl}/">&laquo;</a>
                        </li>
                        <#else>
                        <li class="prev">
                            <a href="/tags/${tag.tagUrl}/page/${posts.number}/">&laquo;</a>
                        </li>
                        </#if>
                    </#if>
                    <li>
                        <a href="#">${posts.number+1}</a>
                    </li>
                    <#if posts.hasNext()>
                        <li class="next">
                            <a href="/tags/${tag.tagUrl}/page/${posts.number+2}">&raquo;</a>
                        </li>
                    </#if>
                </ol>
            </div>
        </#if>
    </div>
</div>
<#include "footer.ftl">
