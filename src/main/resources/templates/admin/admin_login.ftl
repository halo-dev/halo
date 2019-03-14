<#compress >
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <title>${options.blog_title!} | <@spring.message code='login.page.title' /></title>
    <link rel="stylesheet" href="/static/halo-backend/plugins/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="/static/halo-backend/plugins/animate/animate.min.css">
    <link rel="stylesheet" href="/static/halo-backend/plugins/toast/css/jquery.toast.min.css">
    <link rel="stylesheet" href="/static/halo-backend/css/style.min.css">
    <style>
        body{background-color:#f5f5f5}*{outline:0}label{color:#4b1c0f}.loginForm{max-width:380px;margin-top:10%}.loginLogo{font-size:56px;text-align:center;margin-bottom:25px;font-weight:500;color:#444;text-shadow:#b2baba .1em .1em .2em}.loginBody{padding:20px;background-color:#fff;-o-box-shadow:-4px 7px 46px 2px rgba(0,0,0,.1);box-shadow:-4px 7px 46px 2px rgba(0,0,0,.1)}.login-button{background-color:#fff;border-radius:0;border:1px solid #000;transition:all .5s ease-in-out}.login-button:hover{border:1px solid #fff;background-color:#000;color:#fff}.form-group{padding-bottom:25px}#loginName,#loginPwd{border-radius:0}.control{padding-bottom:5px}
    </style>
</head>
<body>
<div class="container loginForm">
    <#-- 虽然Halo使用了宽松的GPL协议，但开发不易，希望您可以保留一下版权声明。笔芯~ -->
    <div class="loginLogo animated fadeInUp">
        Halo
    </div>
    <div class="loginBody animated">
        <form>
            <div class="form-group animated fadeInUp" style="animation-delay: 0.1s">
                <input type="text" class="form-control" name="loginName" id="loginName" placeholder="<@spring.message code='login.form.loginName' />" autocomplete="username">
            </div>
            <div class="form-group animated fadeInUp" style="animation-delay: 0.2s">
                <input type="password" class="form-control" name="loginPwd" id="loginPwd" placeholder="<@spring.message code='login.form.loginPwd' />" autocomplete="current-password">
            </div>
            <#--<div class="row control animated fadeInUp" style="animation-delay: 0.3s">-->
                <#--<div class="col-xs-6">-->
                    <#--<label for="remember"><input type="checkbox" id="remember">  <span style="color: #000;font-weight: lighter">记住我</span></label>-->
                <#--</div>-->
                <#--<div class="col-xs-6 pull-right text-right">-->
                    <#--<a href="#" style="color: #000;">忘记密码？</a>-->
                <#--</div>-->
            <#--</div>-->
            <button type="button" id="btnLogin" data-loading-text="<@spring.message code='login.btn.logining' />" class="btn btn-block login-button animated fadeInUp" onclick="doLogin()"  style="animation-delay: 0.4s;outline: none;"><@spring.message code='login.btn.login' /></button>
        </form>
    </div>
</div>
</body>
<script>
    var heading = "<@spring.message code='common.text.tips' />";
</script>
<script src="/static/halo-common/jquery/jquery.min.js"></script>
<script src="/static/halo-backend/plugins/bootstrap/js/bootstrap.min.js"></script>
<script src="/static/halo-backend/plugins/toast/js/jquery.toast.min.js"></script>
<script src="/static/halo-backend/js/halo.min.js"></script>
<script src="/static/halo-backend/js/login.min.js"></script>
</html>
</#compress>
