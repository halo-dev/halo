<!DOCTYPE html>
<html>

    <#include "_includes/head.ftl">
    <@head title="${post.postTitle} | ${options.blog_title?default('Halo')}" keywords="${options.seo_keywords?default('Halo')}" description="${options.seo_desc?default('Halo')}" canonical="${options.blog_url}/p/${post.postUrl}">
    </@head>

    <body>

    <#include "_includes/header.ftl">
    <@header></@header>

    <div class="content">
        <#include "_includes/jumbotron.ftl">
        <@jumbotron title="${post.postTitle}" date="${post.postDate?string('yyyy-MM-dd')}"></@jumbotron>
        <article class="post container">

            ${post.postContent?if_exists}

            <!-- Comments -->
            <#include "module/comment.ftl">

        </article>
    </div>

    <#include "_includes/footer.ftl">

    </body>

</html>
