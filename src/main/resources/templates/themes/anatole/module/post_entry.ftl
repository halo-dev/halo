<#list posts.content as post>
    <div class="post animated fadeInDown">
        <div class="post-title">
            <h3>
                <a href="${context!}/archives/${post.url}">${post.title}</a>
            </h3>
        </div>
        <div class="post-content">
            <div class="p_part">
                <p>${post.summary!}...</p>
            </div>
            <div class="p_part">
                <p></p>
            </div>
        </div>
        <div class="post-footer">
            <div class="meta">
                <div class="info">
                    <i class="fa fa-sun-o"></i>
                    <span class="date">${post.createTime?string("yyyy-MM-dd")}</span>
                    <i class="fa fa-comment-o"></i>
                    <a href="${context!}/archives/${post.url}#comment_widget">Comments</a>
                    <#if post.tags?size gt 0>
                        <i class="fa fa-tag"></i>
                        <#list post.tags as tag>
                            <a href="${context!}/tags/${tag.slugName}" class="tag">&nbsp;${tag.name}</a>
                        </#list>
                    </#if>
                </div>
            </div>
        </div>
    </div>
</#list>
