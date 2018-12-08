<#include "/common/macro/common_macro.ftl">
<#macro head title,keywords,description,canonical>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <!-- Favicon Icon -->
    <@favicon></@favicon>

    <title>${title}</title>
    <meta name="description" content="${description}">

    <link rel="canonical" href="${canonical}">
    <link rel="alternate" type="application/rss+xml" title="${options.blog_title?default('Halo')}" href="/atom.xml">

    <script type="text/javascript" src="/${themeName}/bower_components/jquery/dist/jquery.min.js"></script>

    <!-- Third-Party CSS -->
    <link rel="stylesheet" href="/${themeName}/bower_components/bootstrap/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="/${themeName}/bower_components/octicons/octicons/octicons.css">
    <link rel="stylesheet" href="/${themeName}/bower_components/hover/css/hover-min.css">
    <link rel="stylesheet" href="/${themeName}/bower_components/primer-markdown/dist/user-content.min.css">
    <link rel="stylesheet" href="/${themeName}/assets/css/syntax.css">

    <!-- My CSS -->
    <link rel="stylesheet" href="/${themeName}/assets/css/common.css">

    <#--<!-- CSS set in page &ndash;&gt;-->
    <#--{% for css in page.css %}-->
    <#--<link rel="stylesheet" href="/${themeName}/assets/css/{{css}}">-->
    <#--{% endfor %}-->

    <#--<!-- CSS set in layout &ndash;&gt;-->
    <#--{% for css in layout.css %}-->
    <#--<link rel="stylesheet" href="/${themeName}/assets/css/{{css}}">-->
    <#--{% endfor %}-->
    <#nested />

    <script type="text/javascript" src="/${themeName}/bower_components/bootstrap/dist/js/bootstrap.min.js"></script>

</head>
</#macro>
