<div class="mdl-color-text--grey-700 mdl-card__supporting-text meta">

    <!-- Author Avatar -->
    <div id="author-avatar">
        <img src="/material/source/img/avatar.png" width="44px" height="44px" alt="Author Avatar"/>
    </div>
    <!-- Author Name & Date -->
    <div>
        <strong>RYAN0UP</strong>
        <span>${post.postDate?string("MM月 dd,yyyy")}</span>
    </div>

    <div class="section-spacer"></div>

    <!-- Favorite -->
    <!--
        <button id="article-functions-like-button" class="mdl-button mdl-js-button mdl-js-ripple-effect mdl-button--icon btn-like">
            <i class="material-icons" role="presentation">favorite</i>
            <span class="visuallyhidden">favorites</span>
        </button>
    -->

    <!-- Qrcode -->
    <!--
    <% if(theme.qrcode.enable === true) { %>
    <%- partial('_widget/qrcode') %>
    <% } %>
    -->
    <!-- Tags (bookmark) -->
    <button id="article-functions-viewtags-button" class="mdl-button mdl-js-button mdl-js-ripple-effect mdl-button--icon">
        <i class="material-icons" role="presentation">bookmark</i>
        <span class="visuallyhidden">bookmark</span>
    </button>
    <ul class="mdl-menu mdl-menu--bottom-right mdl-js-menu mdl-js-ripple-effect" for="article-functions-viewtags-button">
        <li class="mdl-menu__item">
            <a class="post_tag-link" href="/tags/Java/">Java</a></li><li class="mdl-menu__item"><a class="post_tag-link" href="/tags/SpringBoot/">SpringBoot</a>
    </ul>

    <!-- Share -->
    <#include "../_partial/post-info-share.ftl">
</div>
