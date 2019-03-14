<#list posts.content as post>
<!-- Paradox Thumbnail -->
<div class="post_entry-module mdl-card mdl-shadow--${options.theme_material_uiux_card_elevation!2}dp mdl-cell mdl-cell--12-col fade out">
    <!-- Post Thumbnail -->
    <!-- Random Thumbnail -->
    <#if (post.postThumbnail!) == "${options.blog_url}/static/images/thumbnail.png">
    <div class="post_thumbnail-random mdl-card__media mdl-color-text--grey-50">
    <#else>
    <div class="post_thumbnail-custom mdl-card__media mdl-color-text--grey-50 lazy" data-original="${post.postThumbnail!}">
    </#if>
        <!-- Post Title -->
        <p class="article-headline-p"><a href="${options.blog_url!}/archives/${post.postUrl}">${post.postTitle!}</a></p>
    </div>
    <!-- Post Excerpt -->
    <div class="mdl-color-text--grey-600 mdl-card__supporting-text post_entry-content">
        ${post.postSummary!}
        &nbsp;&nbsp;&nbsp;
        <span>
            <a href="${options.blog_url!}/archives/${post.postUrl}" target="_self">阅读全文</a>
        </span>
    </div>
    <!-- Post_entry Info-->
    <div id="post_entry-info">
        <div id="post_entry-left-info" class="mdl-card__supporting-text meta mdl-color-text--grey-600 ">
            <!-- Author Avatar -->
            <div id="author-avatar">
                <img src="${user.userAvatar!'/material/source/img/avatar.png'}" width="44px" height="44px"
                     alt="${user.userDisplayName!'Halo'}'s avatar">
            </div>
            <div>
                <strong>${user.userDisplayName!'Material'}</strong>
                <span>${post.postDate?string("MM月 dd,yyyy")}</span>
            </div>
        </div>
        <div id="post_entry-right-info">
            <!-- Category -->
            <#if post.categories??>
            <#list post.categories as cate>
                <span class="post_entry-category">
                  <a class="post_category-link" href="${options.blog_url!}/categories/${cate.cateUrl}/">${cate.cateName}</a>
                </span>
            </#list>
            </#if>
            <!-- Post Views -->
            <span class="post_entry-views"></span>
        </div>
    </div>
</div>
</#list>
