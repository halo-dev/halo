<!DOCTYPE html>
<html lang="zh">
    <head>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=Edge,chrome=1">
        <meta name="renderer" content="webkit">
        <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
        <title>Halo安装向导</title>
        <link rel="stylesheet" href="/static/plugins/bootstrap/css/bootstrap.min.css">
        <link rel="stylesheet" href="/static/css/AdminLTE.min.css">
        <link rel="stylesheet" href="/static/plugins/animate/animate.min.css">
        <link rel="stylesheet" href="/static/plugins/bootstrapvalidator/css/bootstrapValidator.min.css">
        <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
        <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
        <![endif]-->
        <style>
            body{
                background-color: #f5f5f5;
            }
            .container{
                max-width: 850px;
            }
            .form-horizontal .control-label{
                text-align: left;
            }
            .logo{font-size:56px;text-align:center;margin-bottom:25px;font-weight:500;color:#444;text-shadow:#b2baba .1em .1em .2em;}
        </style>
    </head>
    <body>
        <div class="container">
            <div class="row" style="padding-top: 50px">
                <div class="col-lg-12 col-xs-12">
                    <div class="logo animated fadeInUp">
                        Halo<small style="font-size: 14px;">安装向导</small>
                    </div>
                    <#if isInstall==false>
                    <form method="post" action="/install/do" class="form-horizontal" id="installForm">
                        <div class="box box-solid animated" id="installFirst">
                            <div class="box-body" style="padding: 30px;">
                                <div class="form-group animated fadeInUp" style="animation-delay: 0.1s">
                                    <label for="blogTitle" class="col-sm-4 control-label">网站标题：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="blogTitle" name="blogTitle" value="">
                                    </div>
                                </div>
                                <div class="form-group animated fadeInUp" style="animation-delay: 0.1s">
                                    <label for="blogUrl" class="col-sm-4 control-label">网站地址：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="blogUrl" name="blogUrl" value="">
                                    </div>
                                </div>
                                <div class="form-group animated fadeInUp" style="animation-delay: 0.2s">
                                    <label for="userEmail" class="col-sm-4 control-label">电子邮箱：
                                        <span data-toggle="tooltip" data-placement="top" title="重要，将用于找回密码" style="cursor: pointer">
                                        <i class="fa fa-question-circle" aria-hidden="true"></i>
                                        </span>
                                    </label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="userEmail" name="userEmail" value="">
                                    </div>
                                </div>
                                <div class="form-group animated fadeInUp" style="animation-delay: 0.3s">
                                    <label for="userName" class="col-sm-4 control-label">用户名：
                                        <span data-toggle="tooltip" data-placement="top" title="用于登录后台" style="cursor: pointer">
                                        <i class="fa fa-question-circle" aria-hidden="true"></i>
                                        </span>
                                    </label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="userName" name="userName" value="">
                                    </div>
                                </div>
                                <div class="form-group animated fadeInUp" style="animation-delay: 0.4s">
                                    <label for="userDisplayName" class="col-sm-4 control-label">显示昵称：
                                        <span data-toggle="tooltip" data-placement="top" title="主题或后台显示的名称" style="cursor: pointer">
                                        <i class="fa fa-question-circle" aria-hidden="true"></i>
                                        </span>
                                    </label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="userDisplayName" name="userDisplayName" value="">
                                    </div>
                                </div>
                                <div class="form-group animated fadeInUp" style="animation-delay: 0.5s">
                                    <label for="userPwd" class="col-sm-4 control-label">登录密码：</label>
                                    <div class="col-sm-8">
                                        <input type="password" class="form-control" id="userPwd" name="userPwd" value="">
                                    </div>
                                </div>
                                <div class="form-group animated fadeInUp" style="animation-delay: 0.6s">
                                    <label for="userRePwd" class="col-sm-4 control-label">确认密码：</label>
                                    <div class="col-sm-8">
                                        <input type="password" class="form-control" id="userRePwd" name="userRePwd" value="">
                                    </div>
                                </div>
                            </div>
                            <div class="box-footer" style="padding-right: 30px;">
                                <button type="submit" class="btn btn-primary btn-sm btn-flat pull-right animated fadeInUp" style="animation-delay: 0.9s">安装Halo</button>
                            </div>
                        </div>
                        <div class="box box-solid animated fadeInUp" style="display: none" id="installSuccess">
                            <div class="box-body">
                                <h2>安装成功！</h2>
                                <h4>你可以选择进入前台，或者登陆后台！</h4>
                            </div>
                            <div class="box-footer" style="padding-right: 30px;">
                                <a class="btn btn-primary btn-sm btn-flat" href="/">前台</a>
                                <a class="btn btn-primary btn-sm btn-flat" href="/admin/login">登录后台</a>
                            </div>
                        </div>
                        <div class="box box-solid animated fadeInUp" style="display: none" id="installError">
                            <div class="box-body">
                                <h2>安装失败！</h2>
                                <h4>请返回安装页面尝试重新安装！</h4>
                            </div>
                            <div class="box-footer" style="padding-right: 30px;">
                                <a class="btn btn-primary btn-sm btn-flat" href="/install">返回</a>
                            </div>
                        </div>
                    </form>
                    <#else >
                    <div class="animated fadeInUp" style="animation-delay: 0.1s">
                        <h4>已经安装过了，不能重复安装的酱紫！</h4>
                        <pre style="font: 10px/7px monospace;border: none;">
                                    .'+:
                                  `+@#'##
                                   +#.```#+
                                 `.@` `` .# `
                                  '+`  `  @,`
                                  +'`   ``++
                        ,#@@' `   ++      ;#
                      .@#'..'#`   ,#      .@               ``.,::;;:,,`
                     `#+`    '@   `#`     `@         `.:'##@@###+#++#@@#,
                     .@      `@'   #'     `@  ` `.;+#@@#':..` ``;:    .'@#`
                     ,@       ,@`  ,@`     @`.'@@##;,`` `      ., `     .## `
                     `#        #+   #'`    ##@+,.`           ` +         `#+
                     `#,     ` :#`  .@.   `@.`    .    `      `,`         .#:
                      ;#       `#,   +#`  `` `   `+';+`       :`        `  '#`
                      `@:`      ++`:@@#:         +``  +       '    ,.  ;@#``@,
                     ` ,@`      .@@#:```        .. ##.`:      '`  ;@@.`#@#;`+'
                        ++       @:  ` ` `.`    , ,#@'`'      ;   #@@' +@@:`,#`
                         @;      #:  ` ,'` ,+   ..`@@``:      .`  ;@@:``++  .#`
                        `.@,     ::   ,,  `` #   +``` +`     ``;`` :;   `   .#`
                          ;@.  `      # `:   `.  `+;;+``      `+`           ,@
                         `@@#        `, @@@```'  `  `          `:           ;#``
                        `@+ ;` `     `: @@#` `;```  ` `         ;.          #:
                        @'`           +``:  `..                  +`      ` .#`
                       +#             ,.   ` '                    +.`    ``#'
                      .@.              :'``,+                      ,'.` `.#@. ``
                      #'                 ..`                         `;';;#`
                     .@` `                                            ``:@,``
                   ``#+                                           `    '@,
                     @`                                           `  `#@``
                    ;#                                             .'#'` `
                   `#:                                  ` ` ,;` `,#@+.
                   `@`                                    :;'@+#@#;``  `
                  `.#``                                   '#+#@,.  `
                   ;#                                         @:`
                   #;                                        `:@
                   @.                                          #.
                   @.                                         `+'
                   @.                                          ;#
                   @.                                    :`    .@`
                   @.                                    ;.   ``@`
                   @.                                    ;,  ` `#
                   @,                  `                 +.    `@
                   +'                  ;:                @`     @
                   ,#`                  +,              :;     `@
                  ``@`                   +;          ``,#      .@
                   `#;                    ;#.`      ``'+       ;#`
                    :@ `                  ` '@',..,;#+.      ``#;
                     #:                   ` ```:;;:.`         `@`
                  `` :@                        `    `         ;#``
                     `#+                                      @,
                      `@:`                                   +#`
                       ,#.                                 `,@.`
                      ` ;@.`                               .@:`
                         '@,                              .@;`
                      ```@+#;                         ```;@@  `
                     ` `@' ,@+`                        ,#@.#'
                       ## ```+@;                    `,#@'. `@.
                     `;@ `    ,@#' `      `  `   `;+@@;`    ;@` `
                      #:       `,@@#',` ` ```.;#@@@;. `  `   @@#,
               ``   ;@@`           ;+@@@@@@@@##'.`           ,@@@#:`
                 `+@@@:`           ` ` ```.``   `            `@::#@@'`
               .#@@+++                `                       ,@` .+@@+.
   `    `    :#@@;` @,                                         @,   `+@@#.``;#@@@`
    @@@@#, :@@@: ` ,#                                        ``'#``   `'@@#@@@##@`
   `;''+@@@#@:`    #'                                          .@``    ` ;@@#.
       ,#@@:       @`                                           @:      `.@#@@'`
      '@@'@.      :#                                            '#       `@;`+@#``
    `'#+ :@.    ``#'                                            .@      ``#+` ;' `
     .,  #@``     @`                                             @.     ``:@,
       `.@;      .@                                              ++       `;`
        `,`     `;#                                              :#
                `#'                                               @`
                 @.                                              `@.
                 @`                                               #'`
                `@                                                ;#
                ,#                                                .#
                ;+                                                `@`
                #'                                                 @.
                #:                                                 #:
                @,                                                `++
                @`                                                 ;#`
               `@`                                                 ,@
               `@`                                                 `@  `
               `@`                                                 `@`
               `@`                                                  @`
               `@`                                                  @.
               `@`                                                  #: `
               `@@@@@@@@@##################@@@@@@@@@@@@@@@@@@@@@@@#@@;
             ` `::::::::::::::::::::::::::::,:::::,,,,,,,,,,,,,,,,,:,.`
                    </pre>
                    </div>
                    </#if>
                </div>
            </div>
        </div>
    </body>
    <script src="/static/plugins/jquery/jquery.min.js"></script>
    <script src="/static/plugins/bootstrap/js/bootstrap.min.js"></script>
    <script src="/static/plugins/bootstrapvalidator/js/bootstrapValidator.min.js"></script>
    <script src="/static/plugins/bootstrapvalidator/js/language/zh_CN.js"></script>
    <#if isInstall==false>
    <script>
        var domain = window.location.host;
        $(function () {
            $('[data-toggle="tooltip"]').tooltip()
        });
        $(document).ready(function () {
            $('#blogUrl').val("http://"+domain);
            $('#installForm')
                .bootstrapValidator({
                    message: '安装表单验证失败',
                    feedbackIcons: {
                        valid: 'glyphicon glyphicon-ok',
                        invalid: 'glyphicon glyphicon-remove',
                        validating: 'glyphicon glyphicon-refresh'
                    },
                    fields: {
                        blogTitle: {
                            message: '网站标题验证失败',
                            validators: {
                                notEmpty: {
                                    message: '网站标题不能为空'
                                }
                            }
                        },
                        userEmail: {
                            message: '邮箱验证失败',
                            validators: {
                                notEmpty: {
                                    message: '邮箱不能为空'
                                },
                                emailAddress: {
                                    message: '邮箱格式有误'
                                }
                            }
                        },
                        userName: {
                            message: '用户名验证失败',
                            validators: {
                                notEmpty: {
                                    message: '用户名不能为空'
                                },
                                stringLength: {
                                    min: 1,
                                    max: 24,
                                    message: '用户名超出长度限制'
                                }
                            }
                        },
                        userPwd: {
                            message: '密码验证失败',
                            validators: {
                                notEmpty: {
                                    message: '密码不能为空'
                                },
                                stringLength: {
                                    min: 6,
                                    max: 18,
                                    message: '密码长度必须在6到18位之间'
                                }
                            }
                        },
                        userRePwd: {
                            message: '密码验证失败',
                            validators: {
                                notEmpty: {
                                    message: '确认密码不能为空'
                                },
                                identical: {
                                    field: 'userPwd',
                                    message: '两次输入的密码不相符'
                                }
                            }
                        }
                    }
                })
                .on('success.form.bv',function (e) {
                    e.preventDefault();
                    var $form = $(e.target);
                    var bv = $form.data('bootstrapValidator');
                    $.post($form.attr('action'),$form.serialize(),function (result) {
                        if(result==true){
                            $('#installFirst').hide();
                            $('#installSuccess').show();
                        }else{
                            $('#installFirst').hide();
                            $('#installError').show();
                        }
                    },'json');
                });
        });
    </script>
    <#else>
    <noscript>Not have Script!</noscript>
    </#if>
</html>