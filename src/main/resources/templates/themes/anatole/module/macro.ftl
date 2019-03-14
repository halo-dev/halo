<#include "/common/macro/common_macro.ftl">
<#macro head title,keywords,description>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0"/>
    <title>${title!}</title>
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <meta name="format-detection" content="telephone=no" />
    <meta name="renderer" content="webkit">
    <meta name="theme-color" content="${options.anatole_style_google_color!'#fff'}">
    <meta name="author" content="${user.userDisplayName!}" />
    <meta name="keywords" content="${keywords!}"/>
    <meta name="description" content="${description!}" />
    <@verification></@verification>
    <@favicon></@favicon>
    <link href="/anatole/source/css/font-awesome.min.css" type="text/css" rel="stylesheet"/>
    <link rel="stylesheet" href="/anatole/source/css/blog_basic.min.css?version=88107691fe">
    <link href="/anatole/source/css/style.min.css" type="text/css" rel="stylesheet" />
    <link rel="alternate" type="application/rss+xml" title="atom 1.0" href="/feed.xml">
    <style>
        <#if (options.anatole_style_post_title_lower!'true') == "false">
        .post .post-title h3 {
            text-transform: none;
        }
        </#if>
        <#if (options.anatole_style_blog_title_lower!'true') == "false">
        .sidebar .logo-title .title h3 {
            text-transform: none;
        }
        </#if>
        ::-webkit-scrollbar {
            width: 6px;
            height: 6px;
            background-color: #eee;
        }
        ::-webkit-scrollbar-thumb {
            background-color: ${options.anatole_style_scrollbar!'#3798e8'};
        }
        ::-webkit-scrollbar-track {
            background-color: #eee;
        }
        ${options.anatole_style_self!}
    </style>
</head>
<body>
</#macro>
<#macro footer>
<script type="text/javascript" src="/anatole/source/js/jquery.min.js"></script>
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

    <#if (options.anatole_style_hitokoto!'false')=="true">
	  var xhr = new XMLHttpRequest();
	  xhr.open('get', 'https://v1.hitokoto.cn');
	  xhr.onreadystatechange = function () {
          if (xhr.readyState === 4) {
              var data = JSON.parse(xhr.responseText);
              var yiyan = document.getElementById('yiyan');
              yiyan.innerText = data.hitokoto+"        -「"+data.from+"」";
          }
      };
	  xhr.send();
    </#if>
</script>
<@statistics></@statistics>
</body>
</html>
</#macro>
