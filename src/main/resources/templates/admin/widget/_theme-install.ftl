<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="/static/halo-backend/plugins/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="/static/halo-backend/plugins/toast/css/jquery.toast.min.css">
    <link rel="stylesheet" href="/static/halo-backend/plugins/fileinput/fileinput.min.css">
    <link rel="stylesheet" href="/static/halo-backend/css/AdminLTE.min.css">
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
<script src="/static/halo-common/jquery/jquery.min.js"></script>
<script src="/static/halo-backend/plugins/bootstrap/js/bootstrap.min.js"></script>
<script src="/static/halo-backend/plugins/fileinput/fileinput.min.js"></script>
<#if (options.blog_locale!'zh_CN')=='zh_CN'>
<script src="/static/halo-backend/plugins/fileinput/zh.min.js"></script>
</#if>
<script src="/static/halo-backend/plugins/toast/js/jquery.toast.min.js"></script>
<script src="/static/halo-backend/plugins/layer/layer.js"></script>
<script src="/static/halo-backend/js/halo.min.js"></script>
<script>
    var halo = new $.halo();
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
            if(data.code === 1){
                $("#uploadForm").hide(400);
                halo.showMsgAndParentRedirect(data.msg,'success',1000,'/admin/themes');
            }else{
                halo.showMsg(data.msg,'error',2000);
            }
        });
    }

    /**
     * 拉取主题
     */
    function pullAction() {
        var remoteAddr = $("#remoteAddr").val();
        var themeName = $("#themeName").val();
        var btnInstall = $('#btnInstall');
        if(remoteAddr===null || themeName===null){
            halo.showMsg("<@spring.message code='common.js.info-no-complete' />",'info',2000);
            return;
        }
        btnInstall.button('loading');
        $.post('/admin/themes/clone',{
            'remoteAddr' : remoteAddr,
            'themeName': themeName
        },function (data) {
            if(data.code === 1){
                halo.showMsgAndParentRedirect(data.msg,'success',1000,'/admin/themes');
            }else {
                halo.showMsg(data.msg,'error',2000);
                btnInstall.button('reset');
            }
        },'JSON');
    }
</script>
</html>
