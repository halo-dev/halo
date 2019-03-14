<#list posts.content as post>
<div class="post_entry-module mdl-card mdl-shadow--${options.theme_material_uiux_card_elevation!'2'}dp mdl-cell mdl-cell--12-col fade out">
    <!-- Post_entry Header -->
    <!-- Post Header Info -->
    <div class="post_entry-header_info without-thumbnail">
        <!-- Author Avatar & Name -->
        <img src="${user.userAvatar!'/material/source/img/avatar.png'}" class="avatar-img" width="44px" height="44px" alt="${user.userDisplayName!'Halo'}'s avatar">
        <span class="name-span">RYAN0UP</span>
    </div>
    <!-- Null Thumbnail -->
    <div class="post_thumbnail-null">
    </div>
    <!-- Post_entry Content -->
    <div class="post_entry-content mdl-color-text--grey-600 mdl-card__supporting-text">
        <!-- Post Title -->
        <p class="post_entry-title">
            <a href="${options.blog_url!}/archives/${post.postUrl}">${post.postTitle}</a>
        </p>
        <!-- Post Excerpt -->
        <p class="post_entry-excerpt">
            ${post.postSummary!}...
            &nbsp;&nbsp;&nbsp;
            <span>
                <a href="${options.blog_url!}/archives/${post.postUrl}" target="_self">阅读全文</a>
            </span>
        </p>
        <!-- Post Tags -->
        <ul class="post_entry-tags-list">
            <#if post.tags??>
                <#list post.tags as tag>
                    <li class="post_entry-tags-list-item">
                        <a class="post_entry-tags-list-link" href="${options.blog_url!}/tags/${tag.tagUrl}/">${tag.tagName}</a>
                    </li>
                </#list>
            </#if>
        </ul>
    </div>
    <!-- Post_entry Footer -->
    <div class="post_entry-footer">
        <div class="post_entry-footer-border"></div>
        <div class="post_entry-footer-info">
            <div class="post_entry-footer-date">
                ${post.postDate?string("MM月 dd,yyyy")}
            </div>
            <div class="post_entry-footer-comment">
            </div>
        </div>
    </div>
</div>
</#list>
