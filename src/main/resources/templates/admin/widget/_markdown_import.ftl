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
</head>
<body>
<div class="container-fluid">
    <section class="content">
        <div class="row" id="uploadForm">
            <div class="col-md-12">
                <div class="form-group">
                    <div class="file-loading">
                        <input id="uploadMd" class="file-loading" type="file" name="file" multiple>
                    </div>
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
        $('#uploadMd').fileinput({
            language: 'zh',
            uploadUrl: '/admin/tools/markdownImport',
            allowedFileExtensions: ['md'],
            maxFileCount: 20,
            enctype: 'multipart/form-data',
            dropZoneTitle: '选择 Markdown 文档，最多可同时选择20个',
            showClose: false
        }).on("filebatchuploadcomplete",function (event, files, extra) {
            halo.showMsgAndReload('导入成功！','success',1000);
        });
    }
</script>
</html>
