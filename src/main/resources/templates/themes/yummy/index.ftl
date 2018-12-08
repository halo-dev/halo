<!DOCTYPE html>
<html>

    <#include "_includes/head.ftl">
    <@head title="${options.blog_title?default('Halo')}" keywords="${options.seo_keywords?default('Halo')}" description="${options.seo_desc?default('Halo')}" canonical="${options.blog_url}">
        <link rel="stylesheet" href="/${themeName}/assets/css/index.css">
        <link rel="stylesheet" href="/${themeName}/assets/css/sidebar-popular-repo.css">
    </@head>

    <body>

    <#include "_includes/header.ftl">
    <@header url="/"></@header>

    <div class="content">
        <section class="jumbotron">
            <div class="container">
                <h1>${user.userDesc?default('Yummy Theme')}</h1>
                <div id="jumbotron-meta-info">
                <#if options.yummy_general_location??>
                <span class="meta-info">
                    <span class="octicon octicon-location"></span>
                    ${options.yummy_general_location}
                </span>
                </#if>
                <#if options.yummy_general_company??>
                <span class="meta-info hvr-grow">
                    <span class="octicon octicon-organization"></span>
                    <a href="${options.yummy_general_company_url?default('#')}" target="_blank">${options.yummy_general_company}</a>
                </span>
                </#if>
                <#if options.yummy_general_github_username??>
                <span class="meta-info hvr-grow">
                    <span class="octicon octicon-mark-github"></span>
                    <a href="${options.yummy_general_github_url?default('#')}" target="_blank">${options.yummy_general_github_username}</a>
                </span>
                </#if>
                </div>
            </div>
        </section>
        <section class="content container">

            <div class="row">

                <!-- Post List -->
                <div class="col-md-8">

                    <ol class="post-list">
                    <#list posts.content as post>
                        <li class="post-list-item">
                            <h2 class="post-list-title">
                                <a class="hvr-underline-from-center" href="${options.blog_url}/archives/${post.postUrl}">${post.postTitle}</a>
                            </h2>
                            <p class="post-list-description">
                                ${post.postSummary?if_exists}.
                            </p>
                            <p class="post-list-meta">
                                <span class="octicon octicon-calendar"></span> ${post.postDate?string("yyyy/MM/dd")}
                            </p>
                        </li>
                    </#list>
                    </ol>

                    <!-- Pagination -->
                    <#if posts.totalPages gt 1>
                    <div class="pagination text-align">
                        <div class="btn-group">

                            <#if posts.hasPrevious()>
                                <#if posts.number == 1>
                                <a href="/" class="btn btn-outline">&laquo;</a>
                                <#else>
                                <a href="/page/${posts.number}" class="btn btn-outline">&laquo;</a>
                                </#if>
                            <#else>
                                <button disabled="disabled" href="javascript:;" class="btn btn-outline">&laquo;</button>
                            </#if>

                            <#list rainbow as r>
                                <#if r == posts.number+1>
                                    <a href="javascript:;" class="active btn btn-outline">${r}</a>
                                <#else>
                                    <a href="/page/${r}" class="btn btn-outline">${r}</a>
                                </#if>
                            </#list>

                            <#if posts.hasNext()>
                            <a href="/page/${posts.number+2}" class="btn btn-outline">&raquo;</a>
                            <#else >
                            <button disabled="disabled" href="javascript:;" class="btn btn-outline">&raquo;</button>
                            </#if>

                        </div>
                    </div>
                    </#if>
                </div>

                <div class="col-md-4">
                <#include "_includes/sidebar-popular-repo.ftl">
                </div>

            </div>

        </section>
    </div>

    <#include "_includes/footer.ftl">
    <@footer></@footer>

    </body>

</html>
