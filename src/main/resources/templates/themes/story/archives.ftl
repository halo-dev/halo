<#--
归档页面

@package custom
-->
<#include "header.ftl">
<@header title="Archive - ${options.blog_title?default('Story')}" desc="${options.seo_desc?default('Story')}" keywords="${options.seo_keywords?default('Story')}"></@header>
<div class="container-fluid">
    <div class="row">
        <div id="main" class="col-12 clearfix" role="main">
            <article class="posti" itemscope itemtype="http://schema.org/BlogPosting">
                <!-- 总分类&标签 -->
                <h3>Something</h3>
                <div class="post-tags">
                    <!-- 分类 -->
                    <h4>Category</h4>
                    <ul>
                    <@commonTag method="categories">
                        <#if categories?size gt 0>
                            <#list categories as cate>
                                <li rel="tag"><a href="/categories/${cate.cateUrl}/">${cate.cateName}</a></li>
                            </#list>
                        </#if>
                    </@commonTag>
                    </ul>
                    <!-- 标签 -->
                    <h4>Tag</h4>
                    <ul>
                    <@commonTag method="tags">
                        <#if categories?size gt 0>
                            <#list tags as tag>
                                <li rel="tag"><a href="/tags/${tag.tagUrl}/">${tag.tagName}</a></li>
                            </#list>
                        </#if>
                    </@commonTag>
                    </ul>
                </div>

                <h3>Post</h3>
                <div id="archives">
                <@articleTag method="archivesLess">
                    <#list archivesLess as archive>
                    <h4>${archive.year} 年</h4>
                    <ul>
                        <#list archive.posts?sort_by("postDate")?reverse as post>
                            <li>${post.postDate?string('MM月dd日')}：
                                <a href="/archives/${post.postUrl}">${post.postTitle}</a>
                            </li>
                        </#list>
                    </ul>
                    </#list>
                </@articleTag>
                </div>
            </article>
        </div>
    </div>
</div>
<#include "footer.ftl">
