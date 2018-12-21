<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="/static/halo-backend/plugins/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="/static/halo-backend/plugins/toast/css/jquery.toast.min.css">
    <link rel="stylesheet" href="/static/halo-backend/css/AdminLTE.min.css">
    <style>
        .attachDesc,.attachImg{padding-top:15px;padding-bottom:15px}
        .form-horizontal .control-label{text-align:left}
    </style>
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <div class="col-lg-6 attachImg">
            <img src="${attachment.attachPath!}" style="width: 100%;">
        </div>
        <div class="col-lg-6 attachDesc">
            <div class="box box-solid">
                <div class="box-header with-border">
                    <h3 class="box-title"><@spring.message code='admin.attachments.modal.detail-title' /></h3>
                </div>
                <form class="form-horizontal">
                    <div class="box-body">
                        <div class="form-group">
                            <label for="attachName" class="col-sm-2 control-label"><@spring.message code='admin.attachments.modal.form.attach-name' /></label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="attachName" value="${attachment.attachName!}">
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="attachType" class="col-sm-2 control-label"><@spring.message code='admin.attachments.modal.form.attach-type' /></label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="attachType" value="${attachment.attachType!}" disabled>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="attachPath" class="col-sm-2 control-label"><@spring.message code='admin.attachments.modal.form.attach-path' /></label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="attachPath" value="${attachment.attachPath!}" disabled>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="attachCreated" class="col-sm-2 control-label"><@spring.message code='admin.attachments.modal.form.attach-created' /></label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="attachCreated" value="${attachment.attachCreated!}" disabled>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="attachStorage" class="col-sm-2 control-label"><@spring.message code='admin.attachments.modal.form.attach-storage' /></label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="attachStorage" value="${attachment.attachSize!}" disabled>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="attachSize" class="col-sm-2 control-label"><@spring.message code='admin.attachments.modal.form.attach-size' /></label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="attachSize" value="${attachment.attachWh!}" disabled>
                            </div>
                        </div>
                    </div>
                    <div class="box-footer">
                        <button type="button" class="btn btn-danger btn-sm pull-left" onclick="btn_delete()"><@spring.message code="common.btn.delete" /></button>
                        <button type="button" class="btn btn-info btn-sm pull-right btn-copy" data-clipboard-text="<#if !attachment.attachLocation?? || attachment.attachLocation! == 'SERVER'>${options.blog_url!}</#if>${attachment.attachPath}"><@spring.message code='admin.attachments.modal.form.btn.copy-path' /></button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/static/halo-common/jquery/jquery.min.js"></script>
<script src="/static/halo-backend/plugins/bootstrap/js/bootstrap.min.js"></script>
<script src="/static/halo-backend/plugins/toast/js/jquery.toast.min.js"></script>
<script src="/static/halo-backend/plugins/clipboard/clipboard.min.js"></script>
<script src="/static/halo-backend/js/adminlte.min.js"></script>
<script src="/static/halo-backend/plugins/layer/layer.js"></script>
<script src="/static/halo-backend/js/halo.min.js"></script>
<script>
    var halo = new $.halo();
    function btn_delete() {
        layer.msg('<@spring.message code="common.text.define-delete" />', {
            time: 0
            ,btn: ['<@spring.message code="common.btn.delete" />', '<@spring.message code="common.btn.cancel" />']
            ,yes: function(index){
                layer.close(index);
                $.get('/admin/attachments/remove',{'attachId' : ${attachment.attachId?c}},function (data) {
                    if(data.code === 1){
                        halo.showMsgAndParentRedirect(data.msg,'success',1000,'/admin/attachments');
                    }else{
                        halo.showMsg(data.msg,'error',2000);
                    }
                },'JSON');
            }
        });
    }
    $(document).ready(function(){
        var clipboard = new Clipboard('.btn-copy');
    });
    $('.btn-copy').click(function () {
        halo.showMsg("<@spring.message code='admin.attachments.modal.js.copy-success' />",'success',1000);
    })
</script>
</html>
