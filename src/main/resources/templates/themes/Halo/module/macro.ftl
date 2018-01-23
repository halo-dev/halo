<#-- 公共head -->
<#macro head title="" keywords="" description="">
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="renderer" content="webkit">
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <title>${title}</title>
    <meta name="keywords" content="${keywords}" />
    <meta name="description" content="${description}" />
    <link rel="stylesheet" href="/halo/source/plugins/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="/halo/source/plugins/animate/animate.min.css">
    <link rel="stylesheet" href="/halo/source/plugins/aos/aos.css">
    <link rel="stylesheet" href="/halo/source/plugins/nprogress/nprogress.min.css">
    <link rel="stylesheet" href="/halo/source/plugins/fontawesome/css/font-awesome.min.css">
    <link href='http://fonts.googleapis.com/css?family=Open+Sans:400italic,700italic,700,400&subset=latin,latin-ext' rel='stylesheet' type='text/css' />
    <link rel="stylesheet" href="/halo/source/css/style.css" />
    <script src="/halo/source/plugins/jquery/jquery.min.js"></script>
    <script src="/halo/source/plugins/bootstrap/js/bootstrap.min.js"></script>
</head>
<body style="zoom: 1;">
</#macro>
<#-- 公共底部 -->
<#macro footer>
    <footer class="main-footer text-center">
        <div class="container">
            <div class="footer-content">
                <div class="footer-copyright">
                    <span>©2016 - 2017</span>
                    <span class="with-love">
                        <i class="fa fa-user"></i>
                    </span>
                    <span class="author">RYAN0UP</span>
                </div>
                <div class="footer-powered-by">
                    由 <a href="https://ryanc.cc">Halo</a> 勉强驱动
                </div>
                <div class="footer-theme">
                    主题 -
                    <a href="https://ryanc.cc">
                        Halo
                    </a>
                </div>
            </div>
        </div>
    </footer>
    <script>
        $('.btn-nav').click(function () {
            if($('.navbar-moblie-btn').hasClass('open')){
                $('.navbar-moblie-btn').removeClass('open');
                $('.navbar-panel').addClass('hide');
            }else{
                $('.navbar-moblie-btn').addClass('open');
                $('.navbar-panel').removeClass('hide');
            }
        });
    </script>
</div>
<div class="footer-statistics">
    ${options.statistics_code?if_exists}
</div>
</body>
<#if options.theme_halo_pjax?default("true") == "true">
<script src="/halo/source/plugins/pjax/jquery.pjax.js"></script>
</#if>
<script src="/halo/source/plugins/aos/aos.js"></script>
<script src="/halo/source/plugins/nprogress/nprogress.min.js"></script>
<script>
    $(function() {
        AOS.init();
        NProgress.start();
        setTimeout(function() {
            NProgress.done();
        }, 1000);
    });
    <#if options.theme_halo_pjax?default("true") == "true">
    $(document).pjax('a[target!=_blank]', '.wrapper', {fragment: '.wrapper',timeout: 8000});
    $(document).on('pjax:start', NProgress.start).on('pjax:end', NProgress.done);
    </#if>
</script>
<script>
    NProgress.configure({
        showSpinner: true
    });
    NProgress.start();
    $('#nprogress .bar').css({
        'background': '#000'
    });
    $('#nprogress .peg').css({
        'box-shadow': '0 0 10px #000, 0 0 15px #000'
    });
    $('#nprogress .spinner-icon').css({
        'border-top-color': '#000',
        'border-left-color': '#000'
    });
    setTimeout(function() {
        NProgress.done();
        $('.fade').removeClass('out');
    }, 200);
</script>
</html>
</#macro>