<#compress >
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <title>Halo后台登录</title>
    <link rel="stylesheet" href="/static/plugins/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="/static/plugins/animate/animate.min.css">
    <link rel="stylesheet" href="/static/plugins/toast/css/jquery.toast.min.css">
    <link rel="stylesheet" href="/static/css/style.css">
    <style>
        body{background-color:#f5f5f5}*{outline:0}label{color:#4b1c0f}.login-form{max-width:380px;margin-top:10%}.login-logo{font-size:56px;text-align:center;margin-bottom:25px;font-weight:500;color:#444;text-shadow:#b2baba .1em .1em .2em}.login-body{padding:20px;background-color:#fff;-o-box-shadow:-4px 7px 46px 2px rgba(0,0,0,.1);box-shadow:-4px 7px 46px 2px rgba(0,0,0,.1)}.login-button{background-color:#fff;border-radius:0;border:1px solid #000;transition:all .5s ease-in-out}.login-button:hover{border:1px solid #fff;background-color:#000;color:#fff}.form-group{padding-bottom:25px}#login-name,#login-pwd{border-radius:0}.control{padding-bottom:5px}
    </style>
</head>
<body>
<div class="container login-form animated fadeInDown">
    <div class="login-logo">
        Halo
    </div>
    <div class="login-body">
        <form>
            <div class="form-group">
                <input type="text" class="form-control" name="loginName" id="login-name" placeholder="用户名" value="admin">
            </div>
            <div class="form-group">
                <input type="password" class="form-control" name="loginPwd" id="login-pwd" placeholder="密码" value="123456">
            </div>
            <div class="row control">
                <div class="col-xs-6">
                    <label for="remember"><input type="checkbox" id="remember">  <span style="color: #000;font-weight: lighter">记住我</span></label>
                </div>
                <div class="col-xs-6 pull-right text-right">
                    <a href="#" style="color: #000;">忘记密码？</a>
                </div>
            </div>
            <button type="button" id="btn-login" data-loading-text="登录中..." class="btn btn-block login-button" onclick="btn_login()">登录</button>
        </form>
    </div>
</div>
</body>
<script src="/static/plugins/jquery/jquery.min.js"></script>
<script src="/static/plugins/bootstrap/js/bootstrap.min.js"></script>
<script src="/static/plugins/toast/js/jquery.toast.min.js"></script>
<script src="/static/js/app.js"></script>
<script>
    <@compress single_line=true>
    function btn_login() {
        $('#btn-login').button('loading');
        var name = $("#login-name").val();
        var pwd = $("#login-pwd").val();
        if(name==""||pwd==""){
            showMsg("请输入完整信息！","info",2000);
            $('#btn-login').button('reset');
        }else{
            $.ajax({
                type: 'POST',
                url: '/admin/getLogin',
                async: false,
                data:{
                    'loginName': name,
                    'loginPwd': pwd
                },
                success: function (data) {
                    if(data=="success"){
                        $.toast({
                            text: "登录成功！",
                            heading: '提示',
                            icon: 'success',
                            showHideTransition: 'fade',
                            allowToastClose: true,
                            hideAfter: 1000,
                            stack: 1,
                            position: 'top-center',
                            textAlign: 'left',
                            loader: true,
                            loaderBg: '#ffffff',
                            afterHidden: function () {
                                window.location.href="/admin";
                            }
                        });
                    }else{
                        $.toast({
                            text: "用户名或者密码错误！",
                            heading: '提示',
                            icon: 'error',
                            showHideTransition: 'fade',
                            allowToastClose: true,
                            hideAfter: 2000,
                            stack: 1,
                            position: 'top-center',
                            textAlign: 'left',
                            loader: true,
                            loaderBg: '#ffffff'
                        });
                        $('#btn-login').button('reset');
                    }
                }
            });
        }
    }
    $(document).keydown(function (event) {
       if(event.keyCode == 13){
           btn_login();
       }
    });
    </@compress>
</script>
</html>
</#compress>