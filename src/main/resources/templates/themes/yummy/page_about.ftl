<!DOCTYPE html>
<html>

    <#include "_includes/head.ftl">
    <@head title="${post.postTitle?if_exists} | ${options.blog_title?default('Halo')}" keywords="${options.seo_keywords?default('Halo')}" description="${options.seo_desc?default('Halo')}" canonical="${options.blog_url}">
        <link rel="stylesheet" href="/${themeName}/assets/css/about.css">
        <link rel="stylesheet" href="/${themeName}/assets/css/sidebar-popular-repo.css">
        <link rel="stylesheet" href="/${themeName}/bower_components/flag-icon-css/css/flag-icon.min.css">
    </@head>

    <body>

    <#include "_includes/header.ftl">
    <@header url="/p/about"></@header>

    <div class="content">
        <#include "_includes/jumbotron.ftl">
        <@jumbotron title="${post.postTitle?if_exists}" date="${post.postDate?string('yyyy-MM-dd')}"></@jumbotron>
        <article class="post container">

            <div class="about row">

                <div class="col-md-8">

                    <h2>About me</h2>

                    <p>${post.postContent?if_exists}</p>

                    <h2>Contact</h2>

                    <ul>
                        <li>Email：<a href="mailto:${user.userEmail?if_exists}" target="_top">${user.userEmail?if_exists}</a></li>
                        <li>GitHub：<a href="${options.yummy_general_github_url?if_exists}">${options.yummy_general_github_url?if_exists}</a></li>
                    </ul>

                    <h2>Skill Keywords</h2>

                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h3 class="panel-title">Software Engineer Keywords</h3>
                        </div>
                        <div class="panel-body">

                            <button class="btn btn-default" type="button">Java</button>

                            <button class="btn btn-default" type="button">C</button>

                            <button class="btn btn-default" type="button">C++</button>

                            <button class="btn btn-default" type="button">Qt</button>

                            <button class="btn btn-default" type="button">Python</button>

                            <button class="btn btn-default" type="button">MySQL</button>

                            <button class="btn btn-default" type="button">Oracle</button>

                            <button class="btn btn-default" type="button">SQLite</button>

                            <button class="btn btn-default" type="button">PL/SQL</button>

                            <button class="btn btn-default" type="button">Design Patterns</button>

                        </div>
                    </div>

                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h3 class="panel-title">J2EE Developer Keywords</h3>
                        </div>
                        <div class="panel-body">

                            <button class="btn btn-default" type="button">Spring</button>

                            <button class="btn btn-default" type="button">Struct</button>

                            <button class="btn btn-default" type="button">Hibernet</button>

                            <button class="btn btn-default" type="button">MyBatis</button>

                            <button class="btn btn-default" type="button">JSP</button>

                        </div>
                    </div>

                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h3 class="panel-title">Mobile Developer Keywords</h3>
                        </div>
                        <div class="panel-body">

                            <button class="btn btn-default" type="button">Android</button>

                            <button class="btn btn-default" type="button">Sketch UI Desgin</button>

                        </div>
                    </div>

                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h3 class="panel-title">Web Developer Keywords</h3>
                        </div>
                        <div class="panel-body">

                            <button class="btn btn-default" type="button">HTML</button>

                            <button class="btn btn-default" type="button">CSS</button>

                            <button class="btn btn-default" type="button">JS</button>

                            <button class="btn btn-default" type="button">JQuery</button>

                            <button class="btn btn-default" type="button">Ajax</button>

                            <button class="btn btn-default" type="button">AngularJS</button>

                            <button class="btn btn-default" type="button">NodeJS</button>

                            <button class="btn btn-default" type="button">ExpressJS</button>

                            <button class="btn btn-default" type="button">MongoDB</button>

                            <button class="btn btn-default" type="button">Redis</button>

                            <button class="btn btn-default" type="button">PHP</button>

                            <button class="btn btn-default" type="button">Symfony</button>

                            <button class="btn btn-default" type="button">Boostrap</button>

                        </div>
                    </div>

                    <#include "/common/comment/_native_comment.ftl">

                </div>

                <div class="col-md-4">
                    <#include "_includes/sidebar-popular-repo.ftl">
                </div>

            </div>

        </article>
    </div>

    <#include "_includes/footer.ftl">
    <@footer></@footer>

    </body>

</html>
