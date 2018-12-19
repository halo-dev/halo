<#macro head>
<#import "/common/macro/common_macro.ftl" as common>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <title><#nested ></title>
    <link rel="stylesheet" href="/static/halo-backend/plugins/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="/static/halo-backend/plugins/font-awesome/css/font-awesome.min.css">
    <link rel="stylesheet" href="/static/halo-backend/plugins/pace/pace.min.css">
    <link rel="stylesheet" href="/static/halo-backend/css/AdminLTE.min.css">
    <link rel="stylesheet" href="/static/halo-backend/css/skins/_all-skins.min.css">
    <link rel="stylesheet" href="/static/halo-backend/css/style.min.css">
    <link rel="stylesheet" href="/static/halo-backend/plugins/toast/css/jquery.toast.min.css">
    <link rel="stylesheet" href="/static/halo-backend/plugins/fileinput/fileinput.min.css">
    <link rel="stylesheet" href="/static/halo-common/OwO/OwO.min.css">
    <link rel="stylesheet" href="/static/halo-backend/plugins/pretty-checkbox/pretty-checkbox.min.css">
    <link rel="stylesheet" href="/static/halo-backend/plugins/animate/animate.min.css">
    <link rel="stylesheet" href="//fonts.loli.net/css?family=Source+Sans+Pro:300,400,600,700,300italic,400italic,600italic">
    <!--[if lt IE 9]>
    <script src="//oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
    <script src="//oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
    <script src="/static/halo-common/jquery/jquery.min.js"></script>
    <script src="/static/halo-backend/plugins/bootstrap/js/bootstrap.min.js"></script>
    <script src="/static/halo-common/OwO/OwO.min.js"></script>
</head>
<body class="hold-transition sidebar-mini ${options.admin_theme!'skin-blue'} ${options.admin_layout!''} ${options.sidebar_style!''}">
<div class="wrapper">
<#-- 顶部栏模块 -->
<#include "_header.ftl">
<#-- 菜单栏模块 -->
<#include "_sidebar.ftl">
</#macro>

<#macro footer>
<#include "_footer.ftl">
</div>
<#if (options.admin_pjax!'true') == 'true'>
<script src="/static/halo-backend/plugins/pjax/jquery.pjax.js"></script>
</#if>
<script src="/static/halo-backend/plugins/pace/pace.min.js"></script>
<script src="/static/halo-backend/js/adminlte.min.js"></script>
<script src="/static/halo-backend/plugins/toast/js/jquery.toast.min.js"></script>
<script src="/static/halo-backend/plugins/layer/layer.js"></script>
<script src="/static/halo-backend/plugins/fileinput/fileinput.min.js"></script>
<#if (options.blog_locale!'zh_CN') == 'zh_CN'>
<script src="/static/halo-backend/plugins/fileinput/zh.min.js"></script>
</#if>
<script src="/static/halo-backend/js/halo.min.js"></script>
<@compress single_line=true>
<script>
    var halo = new $.halo();
    $(document).ajaxStart(function() {Pace.restart();});
    <#if (options.admin_pjax!'true') == 'true'>
        $(document).pjax('a[data-pjax=true]', '.content-wrapper', {fragment: '.content-wrapper',timeout: 8000});
    </#if>
    var heading = "<@spring.message code='common.text.tips' />";
</script>
</@compress>
</body>
</html>
</#macro>
