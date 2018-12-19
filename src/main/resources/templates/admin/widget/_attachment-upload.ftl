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
                    <a href="#server" data-toggle="tab">本地</a>
                </li>
                <li>
                    <a href="#url" data-toggle="tab">从URL添加</a>
                </li>
            </ul>
            <div class="tab-content">
                <div class="tab-pane active" id="server">
                    <div class="row" id="uploadForm">
                        <div class="col-md-12">
                            <div class="form-group">
                                <div class="file-loading">
                                    <input id="uploadServer" class="file-loading" type="file" name="file" multiple>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="tab-pane" id="url">
                    <form method="post" class="form-horizontal" id="attachForm">
                        <div class="box-body">
                            <div class="form-group">
                                <label for="attachName" class="col-sm-2 control-label"><@spring.message code='admin.attachments.modal.form.attach-name' /></label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="attachName" name="attachName">
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="attachPath" class="col-sm-2 control-label"><@spring.message code='admin.attachments.modal.form.attach-path' /></label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="attachPath" name="attachPath">
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="attachSmallPath" class="col-sm-2 control-label"><@spring.message code='admin.attachments.modal.form.attach-small-path' /></label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="attachSmallPath" name="attachSmallPath">
                                </div>
                            </div>
                        </div>
                        <div class="box-footer">
                            <button type="button" class="btn btn-primary btn-sm" onclick="addFromUrl()"><@spring.message code='common.btn.define-add' /></button>
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
        $('#uploadServer').fileinput({
            language: 'zh',
            uploadUrl: '/admin/attachments/upload',
            uploadAsync: true,
            allowedFileExtensions: ['jpg','gif','png','jpeg','svg'],
            maxFileCount: 100,
            enctype : 'multipart/form-data',
            showClose: false
        }).on("filebatchuploadcomplete",function (event, files, extra) {
            $("#uploadForm").hide(400);
            halo.showMsgAndReload('上传成功！','success',1000);
        });
    }

    /**
     * 添加外部链接
     */
    function addFromUrl() {
        var param = $("#attachForm").serialize();
        $.ajax({
            type: 'POST',
            url: '/admin/attachments/addFromUrl',
            async: false,
            data: param,
            success: function (data) {
                if(data.code==1){
                    halo.showMsgAndParentRedirect(data.msg,'success',1000,"/admin/attachments");
                }else{
                    halo.showMsg(data.msg,"error",2000);
                }
            }
        });
    }
</script>
</html>
