<#macro head title="">
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <title>${title?default("Halo后台管理")}</title>
    <link rel="stylesheet" href="/static/plugins/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="/static/plugins/font-awesome/css/font-awesome.min.css">
    <link rel="stylesheet" href="/static/plugins/pace/pace.min.css">
    <link rel="stylesheet" href="/static/css/AdminLTE.min.css">
    <link rel="stylesheet" href="/static/css/skins/_all-skins.min.css">
    <link rel="stylesheet" href="/static/css/style.css">
    <link rel="stylesheet" href="/static/css/loader.css">
    <link rel="stylesheet" href="/static/plugins/toast/css/jquery.toast.min.css">
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
    <script src="/static/plugins/jquery/jquery.min.js"></script>
    <script src="/static/plugins/bootstrap/js/bootstrap.min.js"></script>
</head>
<body class="hold-transition sidebar-mini ${options.admin_theme?default('skin-blue')} ${options.admin_layout?default('')} ${options.sidebar_style?default('')}">
<#if options.admin_loading?default("false") == "true">
<!-- 页面加载动画 -->
<div id="loading">
    <div id="loading-center">
        <div id="loading-center-absolute">
            <div class="object" id="object_one"></div>
            <div class="object" id="object_two"></div>
            <div class="object" id="object_three"></div>
            <div class="object" id="object_four"></div>
        </div>
    </div>
</div>
</#if>
</#macro>

<#macro footer>
<#if options.admin_pjax?default("true") == "true">
    <script src="/static/plugins/pjax/jquery.pjax.js"></script>
</#if>
<script src="/static/plugins/pace/pace.min.js"></script>
<script src="/static/js/adminlte.min.js"></script>
<script src="/static/plugins/toast/js/jquery.toast.min.js"></script>
<script src="/static/js/app.js"></script>
<@compress single_line=true>
<script>
    $(document).ajaxStart(function() {Pace.restart();});
    <#if options.admin_pjax?default("true") == "true">
        $(document).pjax('a[data-pjax=true]', '.content-wrapper', {fragment: '.content-wrapper',timeout: 8000});
    </#if>
    <#if options.admin_loading?default("false") == "true">
        $(window).on('load', function(){
            $('body').addClass('loaded');
            setTimeout(function () {
                $('#loading').remove();
            },500);
        });
    </#if>
    $(function () {
        if($(window).width()<1024){
            if($('body').hasClass('layout-boxed')){
                $('body').removeClass('layout-boxed');
            }
            if($('body').hasClass('sidebar-collapse')){
                $('body').removeClass('sidebar-collapse');
            }
        }
    });
</script>
</@compress>
</body>
</html>
</#macro>