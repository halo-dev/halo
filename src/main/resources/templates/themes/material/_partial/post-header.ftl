<#if options.theme_material_scheme?if_exists == "Paradox">
    <!-- Paradox Post Header -->
    <!-- Random Thumbnail -->
    <div class="post_thumbnail-random mdl-card__media mdl-color-text--grey-50">
        <script type="text/ls-javascript" id="post-thumbnail-script">
            var randomNum = Math.floor(Math.random() * 19 + 1);
            $('.post_thumbnail-random').attr('data-original', '/material/source/img/random/material-' + randomNum + '.png');
            $('.post_thumbnail-random').addClass('lazy');
        </script>
        <p class="article-headline-p">
            ${post.postTitle}
        </p>
    </div>
</#if>
<#if options.theme_material_scheme?if_exists == "Isolation">
    <div class="post-header_info without-thumbnail">
        <!-- Author Avatar & Name -->
        <img src="${user.userAvatar?default('/material/source/img/avatar.png')}" class="avatar-img" width="44px" height="44px" alt="${user.userDisplayName?default('halo')}'s avatar">
        <span class="name-span">${user.userDisplayName?default('halo')}</span>
    </div>
</#if>