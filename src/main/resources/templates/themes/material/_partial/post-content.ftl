<div id="post-content" class="mdl-color-text--grey-700 mdl-card__supporting-text fade out">
    <#if (options.theme_material_scheme!'Paradox') == "Paradox">
        ${post.postContent!}
        <#if options.theme_material_other_post_license??>
        <blockquote style="margin: 2em 0 0;padding: 0.5em 1em;border-left: 3px solid #F44336;background-color: #F5F5F5;list-style: none;">
            <p>
                <strong>${options.theme_material_other_post_license!}</strong>
                <br>
                <strong>本文链接：</strong><a href="${options.blog_url!}/archives/${post.postUrl}">${options.blog_url!}/archives/${post.postUrl}</a>
            </p>
        </blockquote>
        </#if>
    </#if>

    <#if (options.theme_material_scheme!'Paradox') == "Isolation">
        <div class="post-content_wrapper">
            <p class="post-title">
                ${post.postTitle!}
            </p>
            ${post.postContent!}
            <#if options.theme_material_other_post_license??>
            <blockquote>
                <p>
                    <strong>${options.theme_material_other_post_license!}</strong>
                    <br>
                    <strong>本文链接：</strong><a href="${options.blog_url!}/archives/${post.postUrl}">${options.blog_url!}/archives/${post.postUrl}</a>
                </p>
            </blockquote>
            </#if>
        </div>
    </#if>
</div>
