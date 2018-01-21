<#list posts.content as post>
    <div class="col-lg-4 col-md-6 col-sm-6 col-xs-12 post-list-item" data-aos="fade-up">
        <div class="post-item-main">
            <a href="/archives/${post.postUrl}">
                <div class="post-item-thumbnail" style="background-image: url(/halo/source/img/pic12.jpg)"></div>
            </a>
            <div class="post-item-info">
                <div class="post-info-title">
                    <a href="/archives/${post.postUrl}"><span>${post.postTitle!}</span></a><br>
                </div>
                <div>
                    <span class="post-info-desc">
                        ${post.postSummary?if_exists}...
                    </span>
                </div>
                <div class="post-info-other" style="text-align: right">
                    <a href="/archives/${post.postUrl}">MORE></a>
                </div>
            </div>
        </div>
    </div>
</#list>