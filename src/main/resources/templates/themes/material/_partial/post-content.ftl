<div id="post-content" class="mdl-color-text--grey-700 mdl-card__supporting-text fade out">
    <#if options.theme_material_scheme?if_exists == "Paradox">
        ${post.postContent?if_exists}
        <blockquote style="margin: 2em 0 0;padding: 0.5em 1em;border-left: 3px solid #F44336;background-color: #F5F5F5;list-style: none;">
            <p>
                <strong></strong>
                <br>
                <strong>本文链接</strong><a href="#">#</a>
            </p>
        </blockquote>
    </#if>

    <#if options.theme_material_scheme?if_exists == "Isolation">
        <div class="post-content_wrapper">
            <p class="post-title">
                ${post.postTitle?if_exists}
            </p>
            ${post.postContent?if_exists}
            <blockquote>
                <p>
                    <strong></strong>
                    <br>
                    <strong>本文链接</strong><a href="#">#</a>
                </p>
            </blockquote>
        </div>
    </#if>
</div>
