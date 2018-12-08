<!DOCTYPE html>
<html>

<#include "_includes/head.ftl">
<@head title="${post.postTitle} | ${options.blog_title?default('Halo')}" keywords="${options.seo_keywords?default('Halo')},${tagWords}" description="${post.postSummary?if_exists}" canonical="${options.blog_url}/archives/${post.postUrl}">
    <link rel="stylesheet" href="/${themeName}/assets/css/sidebar-post-nav.css">
</@head>

<body>

<#include "_includes/header.ftl">
<@header url="/archives/${post.postUrl}"></@header>

<div class="content">
    <#include "_includes/jumbotron.ftl">
    <@jumbotron title="${post.postTitle}" date="${post.postDate?string('yyyy/MM/dd')}"></@jumbotron>
    <article class="post container" itemscope itemtype="http://schema.org/BlogPosting">

        <div class="row">


            <div class="col-md-8 markdown-body">

                ${post.postContent?if_exists}

                <!-- Comments -->
                <#include "/common/comment/_native_comment.ftl">
            </div>

            <div class="col-md-4">
                <#include "_includes/sidebar-post-nav.ftl">
            </div>

        </div>

    </article>
</div>

<#include "_includes/footer.ftl">
<@footer></@footer>

</body>

</html>
