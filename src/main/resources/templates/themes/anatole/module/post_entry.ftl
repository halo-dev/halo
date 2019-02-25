<#list posts.content as post>
    <div class="post animated fadeInDown">
        <div class="post-title">
            <h3>
                <a href="${options.blog_url!}/archives/${post.postUrl}">${post.postTitle}</a>
            </h3>
        </div>
        <div class="post-content">
            <div class="p_part">
                <p>${post.postSummary!}...</p>
            </div>
            <div class="p_part">
                <p></p>
            </div>
        </div>
        <div class="post-footer">
            <div class="meta">
                <div class="info">
                    <i class="fa fa-sun-o"></i>
                    <span class="date">${post.postDate?string("yyyy-MM-dd")}</span>
                    <i class="fa fa-comment-o"></i>
                    <a href="${options.blog_url!}/archives/${post.postUrl}#comment_widget">Comments</a>
                    <#if post.tags?size gt 0>
                        <i class="fa fa-tag"></i>
                        <#list post.tags as tag>
                            <a href="${options.blog_url!}/tags/${tag.tagUrl}" class="tag">&nbsp;${tag.tagName}</a>
                        </#list>
                    </#if>
                </div>
            </div>
        </div>
    </div>
</#list>
