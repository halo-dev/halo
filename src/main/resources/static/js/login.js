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
            success: function (status) {
                if(status=="true"){
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
                }else if(status=="disable"){
                    $('.login-body').addClass('animate shake');
                    $.toast({
                        text: "密码错误已达到5次，请10分钟后再试！",
                        heading: '提示',
                        icon: 'error',
                        showHideTransition: 'fade',
                        allowToastClose: true,
                        hideAfter: 2000,
                        stack: 1,
                        position: 'top-center',
                        textAlign: 'left',
                        loader: true,
                        loaderBg: '#ffffff',
                        afterHidden: function () {
                            $('.login-body').removeClass('animate shake');
                        }
                    });
                    $('#btn-login').button('reset');
                }else{
                    $('.login-body').addClass('animate shake');
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
                        loaderBg: '#ffffff',
                        afterHidden: function () {
                            $('.login-body').removeClass('animate shake');
                        }
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