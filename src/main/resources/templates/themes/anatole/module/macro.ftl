<#import "/common/macro/common_macro.ftl" as common>
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
    <meta name="theme-color" content="${settings.google_color!'#fff'}">
    <meta name="author" content="${user.nickname!}" />
    <meta name="keywords" content="${keywords!}"/>
    <meta name="description" content="${description!}" />
    <@common.globalHeader />
    <link href="${static!}/source/css/font-awesome.min.css" type="text/css" rel="stylesheet"/>
    <link rel="stylesheet" href="${static!}/source/css/blog_basic.min.css?version=88107691fe">
    <link href="${static!}/source/css/style.min.css" type="text/css" rel="stylesheet" />
    <link rel="alternate" type="application/rss+xml" title="atom 1.0" href="/atom.xml">
    <style>
        <#if !settings.post_title_uppper!true>
        .post .post-title h3 {
            text-transform: none;
        }
        </#if>
        <#if !settings.blog_title_uppper!true>
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
            background-color: ${settings.scrollbar!'#3798e8'};
        }
        ::-webkit-scrollbar-track {
            background-color: #eee;
        }
        ${settings.custom!}
    </style>
</head>
<body>
</#macro>
<#macro footer>
<script type="text/javascript" src="${static!}/source/js/jquery.min.js"></script>
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

    <#if settings.hitokoto!false>
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
<@common.statistics />
</body>
</html>
</#macro>
