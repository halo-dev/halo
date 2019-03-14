<#if (options.theme_material_scheme!'Paradox') == "Paradox">
    <!-- Paradox Post Header -->
    <!-- Random Thumbnail -->
    <#if (post.postThumbnail!) == "${options.blog_url}/static/images/thumbnail.png">
    <div class="post_thumbnail-random mdl-card__media mdl-color-text--grey-50">
    <#include "Paradox-post-thumbnail.ftl">
    <#else>
    <div class="post_thumbnail-custom mdl-card__media mdl-color-text--grey-50" style="background-image:url(${post.postThumbnail!})">
    </#if>
        <p class="article-headline-p">
            ${post.postTitle}
        </p>
    </div>
</#if>
<#if (options.theme_material_scheme!'Paradox') == "Isolation">
    <div class="post-header_info without-thumbnail">
        <!-- Author Avatar & Name -->
        <img src="${user.userAvatar!'/material/source/img/avatar.png'}" class="avatar-img" width="44px" height="44px" alt="${user.userDisplayName!'Halo'}'s avatar">
        <span class="name-span">${user.userDisplayName!'Halo'}</span>
    </div>
</#if>
