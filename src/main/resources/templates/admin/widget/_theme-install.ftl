<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="/static/plugins/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="/static/plugins/toast/css/jquery.toast.min.css">
    <link rel="stylesheet" href="/static/plugins/fileinput/fileinput.min.css">
    <link rel="stylesheet" href="/static/css/AdminLTE.min.css">
    <style type="text/css" rel="stylesheet">
        .form-horizontal .control-label{
            text-align: left;
        }
        .alert-info{
            color: #31708f!important;
            background-color: #d9edf7!important;
            border-color: #bce8f1!important;
        }
    </style>
</head>
<body>
<div class="container-fluid">
    <section class="content">
        <div class="nav-tabs-custom">
            <ul class="nav nav-tabs">
                <li class="active">
                    <a href="#upload" data-toggle="tab"><@spring.message code='admin.themes.modal.install.tab.upload' /></a>
                </li>
                <li>
                    <a href="#clone" data-toggle="tab"><@spring.message code='admin.themes.modal.install.tab.pull' /></a>
                </li>
            </ul>
            <div class="tab-content">
                <div class="tab-pane active" id="upload">
                    <div class="row" id="uploadForm">
                        <div class="col-md-12">
                            <div class="form-group">
                                <div class="file-loading">
                                    <input id="uploadTheme" class="file-loading" type="file" name="file" multiple>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="tab-pane" id="clone">
                    <form method="post" class="form-horizontal" id="pullForm">
                        <div class="box-body">
                            <div class="alert alert-info alert-dismissible" role="alert">
                                <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                                <strong><@spring.message code='common.text.tips' /></strong> <@spring.message code='admin.themes.modal.install.tips' />&nbsp;<a href="https://gitee.com/babyrui" target="_blank" class="alert-link">https://gitee.com/babyrui</a>.
                            </div>
                            <div class="form-group">
                                <label for="remoteAddr" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.themes.modal.install.form.remote-address' /></label>
                                <div class="col-lg-4 col-sm-8">
                                    <input type="text" class="form-control" id="remoteAddr" name="remoteAddr">
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="themeName" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.themes.modal.install.form.theme-name' /></label>
                                <div class="col-lg-4 col-sm-8">
                                    <input type="text" class="form-control" id="themeName" name="themeName">
                                </div>
                            </div>
                        </div>
                        <div class="box-footer">
                            <button type="button" data-loading-text="<@spring.message code='admin.themes.modal.install.btn.installing' />" class="btn btn-primary btn-sm" onclick="pullAction()" id="btnInstall"><@spring.message code='admin.themes.modal.install.btn.install' /></button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </section>
</div>
</body>
<script src="/static/plugins/jquery/jquery.min.js"></script>
<script src="/static/plugins/bootstrap/js/bootstrap.min.js"></script>
<script src="/static/plugins/fileinput/fileinput.min.js"></script>
<#if options.blog_locale?default('zh_CN')=='zh_CN'>
<script src="/static/plugins/fileinput/zh.min.js"></script>
</#if>
<script src="/static/plugins/toast/js/jquery.toast.min.js"></script>
<script src="/static/plugins/layer/layer.js"></script>
<script src="/static/js/app.js"></script>
<script>
    $(document).ready(function () {
        loadFileInput();
    });

    /**
     * 初始化上传组件
     */
    function loadFileInput() {
        $('#uploadTheme').fileinput({
            language: 'zh',
            uploadUrl: '/admin/themes/upload',
            allowedFileExtensions: ['zip'],
            maxFileCount: 1,
            enctype: 'multipart/form-data',
            dropZoneTitle: '<@spring.message code="admin.themes.modal.install.plugin.text" />',
            showClose: false
        }).on("fileuploaded",function (event,data,previewId,index) {
            var data = data.jqXHR.responseJSON;
            if(data.code==1){
                $("#uploadForm").hide(400);
                $.toast({
                    text: data.msg,
                    heading: '<@spring.message code="common.text.tips" />',
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
                        parent.location.href="/admin/themes";
                    }
                });
            }else{
                $.toast({
                    text: data.msg,
                    heading: '<@spring.message code="common.text.tips" />',
                    icon: 'error',
                    showHideTransition: 'fade',
                    allowToastClose: true,
                    hideAfter: 1000,
                    stack: 1,
                    position: 'top-center',
                    textAlign: 'left',
                    loader: true,
                    loaderBg: '#ffffff'
                });
            }
        });
    }

    /**
     * 拉取主题
     */
    function pullAction() {
        var remoteAddr = $("#remoteAddr").val();
        var themeName = $("#themeName").val();
        if(remoteAddr==null || themeName==null){
            $.toast({
                text: "<@spring.message code='common.js.info-no-complete' />",
                heading: '<@spring.message code="common.text.tips" />',
                icon: 'error',
                showHideTransition: 'fade',
                allowToastClose: true,
                hideAfter: 1000,
                stack: 1,
                position: 'top-center',
                textAlign: 'left',
                loader: true,
                loaderBg: '#ffffff'
            });
            return;
        }
        $('#btnInstall').button('loading');
        $.ajax({
            type: 'post',
            url: '/admin/themes/clone',
            data: {
                remoteAddr : remoteAddr,
                themeName: themeName
            },
            success: function (data) {
                if(data.code==1){
                    $.toast({
                        text: data.msg,
                        heading: '<@spring.message code="common.text.tips" />',
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
                            parent.location.href="/admin/themes";
                        }
                    });
                }else {
                    showMsg(data.msg,"error",1000);
                    $('#btnInstall').button('reset');
                }
            }
        });
    }
</script>
</html>
