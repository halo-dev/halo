<#compress >
<#include "../module/_macro.ftl">
<@head>${options.blog_title!} | 500</@head>
<div class="content-wrapper">
    <!-- Content Header (Page header) -->
    <section class="content-header">
        <h1>
            500 Error Page
        </h1>
        <ol class="breadcrumb">
            <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
            <li><a href="#">Error</a></li>
            <li class="active">500 error</li>
        </ol>
    </section>

    <!-- Main content -->
    <section class="content">
        <div class="error-page">
            <h2 class="headline text-yellow"> 500</h2>

            <div class="error-content">
                <h3><i class="fa fa-warning text-yellow"></i> Oops! Something went wrong.</h3>

                <p>
                    We could not find the page you were looking for.
                    Meanwhile, you may <a href="/admin/">return to dashboard</a> or try using the search form.
                </p>
            </div>
        </div>
    </section>
</div>
<@footer>
    <script type="application/javascript" id="footer_script">

    </script>
</@footer>
</#compress>
