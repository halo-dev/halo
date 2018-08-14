<#include "header.ftl">
<@header title="${post.postTitle?if_exists} - ${options.blog_title?default('Story')}" desc="${post.postSummary?if_exists}" keywords="${options.seo_keywords?default('Story')},${tagWords}"></@header>
<div class="container-fluid">
    <div class="row">
        <div id="main" class="col-12 clearfix" role="main">
            <article class="posti" itemscope itemtype="http://schema.org/BlogPosting">
                <h1 class="post-title" itemprop="name headline">${post.postTitle}</h1>
                <div class="post-meta">
                    <p>Written by <a itemprop="name" href="${options.blog_url?default('#')}" rel="author">${user.userDisplayName?if_exists}</a> with ♥ on <time datetime="${post.postDate}" itemprop="datePublished">${post.postDate?string('MMM d,yyyy')}</time><#if post.categories?size gt 0> in <#list post.categories as cate><a href="/categories/${cate.cateUrl}">${cate.cateName}</a></#list></#if></p>
                </div>

                <div class="post-content" itemprop="articleBody">
                    ${post.postContent?if_exists}
                </div>

                <div style="display:block;" class="clearfix">
                    <section style="float:left;">
                        <span itemprop="keywords" class="tags">
                            tag(s): <#if post.tags?size gt 0><#list post.tags as tag><a href="/tags/${tag.tagUrl}">${tag.tagName}</a>&nbsp;</#list></#if>
                        </span>
                    </section>
                    <section style="float:right;">
                        <span><a id="btn-comments" href="javascript:isComments();">show comments</a></span> · <span><a href="javascript:goBack();">back</a></span> ·
                        <span><a href="${options.blog_url?default('#')}">home</a></span>
                    </section>
                </div>
                <#include "comments.ftl">
            </article>
        </div>
    </div>
</div>
<#include "footer.ftl">
