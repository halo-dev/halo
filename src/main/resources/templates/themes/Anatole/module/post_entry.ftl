<#list posts.content as post>
    <div class="post animated fadeInDown">
        <div class="post-title">
            <h3>
                <a href="/post/${post.postUrl}">${post.postTitle}</a>
            </h3>
        </div>
        <div class="post-content">
            <div class="p_part">
                <p>${post.postSummary?if_exists}...</p>
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
                    <a href="/post/${post.postUrl}#comment_widget">Comments</a>
                    <i class="fa fa-tag"></i>
                    <a href="/tags/App" class="tag">&nbsp;App</a>
                    <a href="/tags/Evernote" class="tag">&nbsp;Evernote</a>
                    <a href="/tags/Mac" class="tag">&nbsp;Mac</a>
                </div>
            </div>
        </div>
    </div>
</#list>