<#macro title hasLink=="">
    <#if hasLink == "true">
        <h3 class="post-title" itemprop="name">
            <a class="post-title-link" href="${post.postUrl}">${post.postTitle}</a>
        </h3>
    <#else >
        <h3 class="post-title" itemprop="name">
            ${post.postTitle}
        </h3>
    </#if>
</#macro>

