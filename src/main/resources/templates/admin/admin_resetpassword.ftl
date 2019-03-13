<#compress >
    <!DOCTYPE html>
    <html lang="zh">
    <head>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
        <title>${options.blog_title!} | 重置密码</title>
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

            .reset-button {
                background-color: #fff;
                border-radius: 0;
                border: 1px solid #000;
                transition: all .5s ease-in-out
            }

            .reset-button:hover {
                border: 1px solid #fff;
                background-color: #000;
                color: #fff
            }

            .form-group {
                margin-bottom: 32px;
            }

            #password,#definePassword {
                border-radius: 0
            }
        </style>
    </head>
    <body>
    <div class="container findPasswordForm">
        <div class="findPasswordLogo animated fadeInUp">
            Halo <small style="font-size: 14px;">重置密码</small>
        </div>
        <#if isRight>
        <div class="findPasswordBody animated">
            <form>
                <div class="form-group animated fadeInUp" style="animation-delay: 0.1s">
                    <input type="password" class="form-control" name="password" id="password" placeholder="新密码">
                </div>
                <div class="form-group animated fadeInUp" style="animation-delay: 0.2s">
                    <input type="password" class="form-control" name="definePassword" id="definePassword" placeholder="确认新密码">
                </div>
                <button type="button" id="btnResetPassword" onclick="resetPassword()" class="btn btn-block reset-button animated fadeInUp" style="animation-delay: 0.3s;outline: none;">重置密码</button>
            </form>
        </div>
        <#else>
        <div class="alert alert-info animated fadeInUp" role="alert" style="animation-delay: 0.1s">该链接已失效，请重新上一步操作</div>
        </#if>
    </div>
    </body>
    <#if isRight>
    <script src="/static/halo-common/jquery/jquery.min.js"></script>
    <script src="/static/halo-admin/plugins/bootstrap/js/bootstrap.min.js"></script>
    <script src="/static/halo-admin/plugins/toast/js/jquery.toast.min.js"></script>
    <script src="/static/halo-admin/js/halo.min.js"></script>
    <script>
        var halo = new $.halo();
        var heading = "<@spring.message code='common.text.tips' />";
        function resetPassword() {
            var password = $('#password');
            var definePassword = $('#definePassword');
            if (password.val() === "" || definePassword.val() === "") {
                halo.showMsg("请输入完整信息！", 'info', 2000);
            } else {
                $.ajax({
                    url: '/admin/resetPassword',
                    dataType: 'JSON',
                    type: 'post',
                    contentType: 'application/json',
                    data: JSON.stringify({
                        "password": password.val(),
                        "definePassword": definePassword.val(),
                        'code': '${code}'
                    }),
                    processData: false,
                    success: function( data, textStatus, jQxhr ){
                        if (data.code === 1) {
                            halo.showMsgAndRedirect(data.msg,'success',1000,"/admin/login")
                        } else {
                            halo.showMsg(data.msg,'error',2000);
                        }
                    },
                    error: function( jqXhr, textStatus, errorThrown ){
                        console.log( errorThrown );
                    }
                });
            }
        }
    </script>
    </#if>
    </html>
</#compress>
