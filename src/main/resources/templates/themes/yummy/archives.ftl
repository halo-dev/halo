<!DOCTYPE html>
<html>

    <#include "_includes/head.ftl">
    <@head title="归档 | ${options.blog_title?default('Halo')}" keywords="${options.seo_keywords?default('Halo')}" description="${options.seo_desc?default('Halo')}" canonical="${options.blog_url}/archives">
        <link rel="stylesheet" href="/${themeName}/assets/css/blog-page.css">
    </@head>

    <body>

    <#include "_includes/header.ftl">
    <@header url="/archives"></@header>

    <div class="content">
        <#include "_includes/jumbotron.ftl">
        <@jumbotron title="归档" date="${options.blog_start}"></@jumbotron>
        <article class="post container" itemscope itemtype="http://schema.org/BlogPosting">

            <div class="row">

                <div class="col-md-8">

                    <!-- Blog list -->
                    <ul id="posts-list">
                        <@articleTag method="archivesLess">
                            <#list archivesLess as archive>
                                <#list archive.posts?sort_by("postDate")?reverse as post>
                                    <li class="posts-list-item">
                                        <div class="posts-content">
                                            <span class="posts-list-meta">${post.postDate?string("yyyy-MM-dd")}</span>
                                            <a class="posts-list-name bubble-float-left" href="${options.blog_url}/archives/${post.postUrl}">${post.postTitle}</a>
                                            <span class='circle'></span>
                                        </div>
                                    </li>
                                </#list>
                            </#list>
                        </@articleTag>
                    </ul>

                    <!-- Pagination -->

                </div>

                <div class="col-md-4">
                    <#include "_includes/sidebar-post-tag.ftl">
                </div>

            </div>
            <script>
                $(document).ready(function(){

                    // Enable bootstrap tooltip
                    $("body").tooltip({ selector: '[data-toggle=tooltip]' });

                });
            </script>

        </article>
    </div>

    <#include "_includes/footer.ftl">
    <@footer></@footer>

    </body>

</html>
