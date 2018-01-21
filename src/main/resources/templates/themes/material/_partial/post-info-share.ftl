<button id="article-fuctions-share-button" class="mdl-button mdl-js-button mdl-js-ripple-effect mdl-button--icon">
    <i class="material-icons" role="presentation">share</i>
    <span class="visuallyhidden">share</span>
</button>
<ul class="mdl-menu mdl-menu--bottom-right mdl-js-menu mdl-js-ripple-effect" for="article-fuctions-share-button">
    <!-- Share Weibo -->
    <#if options.theme_material_sns_share_weibo?if_exists=='true'>
    <a class="post_share-link" href="http://service.weibo.com/share/share.php?appkey=&title=<%= page.title %>&url=<%= config.url + url_for(path) %>&pic=<%- config.url + theme.head.favicon %>&searchPic=false&style=simple" target="_blank">
        <li class="mdl-menu__item">
            分享到Weibo
        </li>
    </a>
    </#if>

    <!-- Share Twitter -->
    <#if options.theme_material_sns_share_twitter?if_exists=='true'>
    <a class="post_share-link" href="https://twitter.com/intent/tweet?text=<%= page.title %>&url=<%= config.url + url_for(path) %>&via=<%= config.author %>" target="_blank">
        <li class="mdl-menu__item">
            分享到Twitter
        </li>
    </a>
    </#if>

    <!-- Share Facebook -->
    <#if options.theme_material_sns_share_facebook?if_exists=='true'>
    <a class="post_share-link" href="https://www.facebook.com/sharer/sharer.php?u=<%= config.url + url_for(path) %>" target="_blank">
        <li class="mdl-menu__item">
            分享到FaceBook
        </li>
    </a>
    </#if>

    <!-- Share Google+ -->
    <#if options.theme_material_sns_share_googleplus?if_exists=='true'>
    <a class="post_share-link" href="https://plus.google.com/share?url=<%= config.url + url_for(path) %>" target="_blank">
        <li class="mdl-menu__item">
            分享到Google+
        </li>
    </a>
    </#if>

    <!-- Share LinkedIn -->
    <#if options.theme_material_sns_share_linkedin?if_exists=='true'>
    <a class="post_share-link" href="https://www.linkedin.com/shareArticle?mini=true&url=<%- config.url + url_for(path) %>&title=<%= page.title %>" target="_blank">
        <li class="mdl-menu__item">
            分享到LinkedIn
        </li>
    </a>
    </#if>

    <!-- Share QQ -->
    <#if options.theme_material_sns_share_qq?if_exists=='true'>
    <a class="post_share-link" href="http://connect.qq.com/widget/shareqq/index.html?site=<%= config.title %>&title=<%= page.title %>&summary=<%= config.description %>&pics=<%- config.url + theme.head.favicon %>&url=<%- config.url +  url_for(path) %>" target="_blank">
        <li class="mdl-menu__item">
            分享到QQ
        </li>
    </a>
    </#if>

    <!-- Share Telegram -->
    <#if options.theme_material_sns_share_telegram?if_exists=='true'>
    <a class="post_share-link" href="https://telegram.me/share/url?url=<%- config.url + url_for(path) %>&text=<%= page.title %>" target="_blank">
        <li class="mdl-menu__item">
            分享到Telegram
        </li>
    </a>
    </#if>
</ul>
