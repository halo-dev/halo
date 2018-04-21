<#macro post post="">
    <article id="${post.postUrl}"
             class="article-card article-type-${post}" itemprop="blogPost">

        <div class="post-meta">
        <#include "post/date.ftl">
        <#include "post/category.ftl">
        </div>

        <#include "post/title.ftl">
        <@title hasLink="true"></@title>

        <div class="post-content" id="post-content" itemprop="postContent">
            ${post.postSummary?if_exists}
            <a href="${post.postUrl}" class="post-more waves-effect waves-button">
                继续阅读...
            </a>
        </div>
        <#if post.tags?size gt 0>
            <div class="post-footer">
                <#include "post/tag.ftl">
            </div>
        </#if>
    </article>
</#macro>
