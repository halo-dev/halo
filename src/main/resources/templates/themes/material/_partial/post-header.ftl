<#if (options.theme_material_scheme!'Paradox') == "Paradox">
    <!-- Paradox Post Header -->
    <!-- Random Thumbnail -->
    <#if (post.thumbnail!) == "${options.blog_url}/static/halo-content/images/thumbnail.png">
    <div class="post_thumbnail-random mdl-card__media mdl-color-text--grey-50">
    <#include "Paradox-post-thumbnail.ftl">
    <#else>
    <div class="post_thumbnail-custom mdl-card__media mdl-color-text--grey-50" style="background-image:url(${post.thumbnail!})">
    </#if>
        <p class="article-headline-p">
            ${post.title}
        </p>
    </div>
</#if>
<#if (options.theme_material_scheme!'Paradox') == "Isolation">
    <div class="post-header_info without-thumbnail">
        <!-- Author Avatar & Name -->
        <img src="${user.avatar!'/material/source/img/avatar.png'}" class="avatar-img" width="44px" height="44px" alt="${user.nickName!'Halo'}'s avatar">
        <span class="name-span">${user.nickName!'Halo'}</span>
    </div>
</#if>
