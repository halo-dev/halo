<#macro head title="" keywords="" description="">
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport" />
    <title>${title?default("Anatole")}</title>
    <meta content="yes" name="apple-mobile-web-app-capable" />
    <meta content="black" name="apple-mobile-web-app-status-bar-style" />
    <meta content="telephone=no" name="format-detection" />
    <meta name="renderer" content="webkit">
    <meta name="author" content="" />
    <meta name="keywords" content="${keywords?default("Anatole")}"/>
    <meta name="description" content="${description?default("Anatole")}" />
    <link rel="shortcut icon" href="/anatole/source/images/favicon.png" type="image/x-icon" />
    <link href="/anatole/source/css/font-awesome.min.css" type="text/css" rel="stylesheet"/>
    <link rel="stylesheet" href="/anatole/source/css/blog_basic.css?version=88107691fe">
    <link href="/anatole/source/css/style.css" type="text/css" rel="stylesheet" />
    <link rel="alternate" type="application/rss+xml" title="atom 1.0" href="/feed.xml">
</head>
<body>
</#macro>
<#macro footer>
<script type="text/javascript" src="/anatole/source/js/jquery.js"></script>
<script type="text/javascript" src="/anatole/source/js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="/anatole/source/js/jquery.appear.js"></script>
<script type="text/javascript">
    var url = location.href;
    var urlstatus = false;
    $(".nav li a").each(function () {
        if ((url + '/').indexOf($(this).attr('href')) > -1 && $(this).attr('href') != '/') {
            $(this).addClass('current');
            urlstatus = true;
        } else {
            $(this).removeClass('current');
        }
    });
    if (!urlstatus) {
        $(".nav li a").eq(0).addClass('current');
    }
</script>
</body>
</html>
</#macro>