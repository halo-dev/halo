<#compress >
    <!DOCTYPE html>
    <html lang="zh">
    <head>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
        <title>${options.blog_title!} | 找回密码</title>
        <link rel="stylesheet" href="/static/halo-admin/plugins/bootstrap/css/bootstrap.min.css">
        <link rel="stylesheet" href="/static/halo-admin/plugins/animate/animate.min.css">
        <link rel="stylesheet" href="/static/halo-admin/plugins/toast/css/jquery.toast.min.css">
        <link rel="stylesheet" href="/static/halo-admin/css/style.min.css">
        <style>
            body {
                background-color: #f5f5f5
            }

            * {
                outline: 0
            }

            label {
                color: #4b1c0f
            }

            .findPasswordForm {
                max-width: 380px;
                margin-top: 10%
            }

            .findPasswordLogo {
                font-size: 56px;
                text-align: center;
                margin-bottom: 25px;
                font-weight: 500;
                color: #444;
                text-shadow: #b2baba .1em .1em .2em
            }

            .findPasswordBody {
                padding: 20px;
                background-color: #fff;
                -o-box-shadow: -4px 7px 46px 2px rgba(0,0,0,.1);
                box-shadow: -4px 7px 46px 2px rgba(0,0,0,.1)
            }

            .alert{
                -o-box-shadow: -4px 7px 46px 2px rgba(0,0,0,.1);
                box-shadow: -4px 7px 46px 2px rgba(0,0,0,.1)
            }

            .send-button {
                background-color: #fff;
                border-radius: 0;
                border: 1px solid #000;
                transition: all .5s ease-in-out
            }

            .send-button:hover {
                border: 1px solid #fff;
                background-color: #000;
                color: #fff
            }

            .form-group {
                margin-bottom: 32px;
            }

            #userName,#email {
                border-radius: 0
            }
        </style>
    </head>
    <body>
    <div class="container findPasswordForm">
        <div class="findPasswordLogo animated fadeInUp">
            Halo <small style="font-size: 14px;">找回密码</small>
        </div>
        <div class="alert alert-info animated fadeInUp" role="alert" style="animation-delay: 0.1s">
            请输入您的用户名和电子邮箱地址。您会收到一封包含创建新密码链接的电子邮件（请确定已经配置好了发信服务器信息）。
        </div>
        <div class="findPasswordBody animated">
            <form>
                <div class="form-group animated fadeInUp" style="animation-delay: 0.2s">
                    <input type="text" class="form-control" name="userName" id="userName" placeholder="用户名">
                </div>
                <div class="form-group animated fadeInUp" style="animation-delay: 0.3s">
                    <input type="text" class="form-control" name="email" id="email" placeholder="电子邮箱地址">
                </div>
                <button type="button" id="btnFindPassword" data-loading-text="发送中..." onclick="findPassword()" class="btn btn-block send-button animated fadeInUp" style="animation-delay: 0.4s;outline: none;">发送邮件</button>
            </form>
        </div>
    </div>
    </body>
    <script src="/static/halo-common/jquery/jquery.min.js"></script>
    <script src="/static/halo-admin/plugins/bootstrap/js/bootstrap.min.js"></script>
    <script src="/static/halo-admin/plugins/toast/js/jquery.toast.min.js"></script>
    <script src="/static/halo-admin/js/halo.min.js"></script>
    <script>
        var halo = new $.halo();
        var heading = "<@spring.message code='common.text.tips' />";
        function findPassword() {
            var btnFindPassword = $('#btnFindPassword');
            var name = $("#userName");
            var email = $("#email");
            btnFindPassword.button('loading');
            if (email.val() === "" || name.val() === "") {
                halo.showMsg("请输入完整信息！", 'info', 2000);
                btnFindPassword.button('reset');
            } else {
                $.post('/admin/sendResetPasswordEmail',{
                    'userName': name.val(),
                    'email': email.val()
                },function (data) {
                    if (data.code === 1) {
                        halo.showMsgAndRedirect(data.msg,'success',1000,"/admin/login")
                    } else {
                        halo.showMsg(data.msg,'error',2000);
                    }
                    btnFindPassword.button('reset');
                },'JSON');
            }
        }
    </script>
    </html>
</#compress>
