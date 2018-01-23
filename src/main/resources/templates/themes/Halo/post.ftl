<#include "module/macro.ftl">
<@head title="${post.postTitle?if_exists} | ${options.site_title?if_exists}" keywords="${options.seo_keywords?if_exists}" description="${options.seo_desc?if_exists}"></@head>
<div class="wrapper" style="height: auto; min-height: 100%;">
<#include "module/header.ftl">
<div class="main-content">
    <section class="container">
        <div class="post-reading-main" style="background-color: #fff" data-aos="fade-up">
            <div class="post-infos" style="text-align: center">
                <h1>${post.postTitle}</h1>
                <span>${user.userDisplayName?if_exists},${post.postDate?if_exists?string("yyyy-MM-dd HH:mm")}</span>
            </div>
            <article class="post-content">
                ${post.postContent}
            </article>
            <div class="comment" id="comment">
                <#include "module/comment.ftl">
            </div>
        </div>
    </section>
</div>
<@footer></@footer>