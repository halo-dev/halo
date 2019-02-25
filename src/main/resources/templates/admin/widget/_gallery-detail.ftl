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
            <img src="${gallery.galleryUrl!}" style="width: 100%;">
        </div>
        <div class="col-lg-6 attachDesc">
            <div class="box box-solid">
                <div class="box-header with-border">
                    <h3 class="box-title"><@spring.message code='admin.pages.galleries.modal.title' /></h3>
                </div>
                <form action="/admin/page/gallery/save" method="post" class="form-horizontal" id="galleryForm">
                    <div class="box-body">
                        <input type="hidden" value="${gallery.galleryId?c}" name="galleryId">
                        <div class="form-group">
                            <label for="galleryName" class="col-sm-2 control-label"><@spring.message code='admin.pages.galleries.form.gallery-name' /></label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="galleryName" name="galleryName" value="${gallery.galleryName}">
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="galleryDesc" class="col-sm-2 control-label"><@spring.message code='admin.pages.galleries.form.gallery-desc' /></label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="galleryDesc" name="galleryDesc" value="${gallery.galleryDesc!}" >
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="galleryDate" class="col-sm-2 control-label"><@spring.message code='admin.pages.galleries.form.gallery-date' /></label>
                            <div class="col-sm-10">
                                <input type="date" class="form-control" id="galleryDate" name="galleryDate" value="${gallery.galleryDate!}">
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="galleryLocation" class="col-sm-2 control-label"><@spring.message code='admin.pages.galleries.form.gallery-location' /></label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="galleryLocation" name="galleryLocation" value="${gallery.galleryLocation!}" >
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="galleryUrl" class="col-sm-2 control-label"><@spring.message code='admin.pages.galleries.form.gallery-url' /></label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="galleryUrl" name="galleryUrl" value="${gallery.galleryUrl}">
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="galleryUrl" class="col-sm-2 control-label"><@spring.message code='admin.pages.galleries.form.gallery-thumbnail-url' /></label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="galleryThumbnailUrl" name="galleryThumbnailUrl" value="${gallery.galleryThumbnailUrl}">
                            </div>
                        </div>
                    </div>
                    <div class="box-footer">
                        <button type="button" class="btn btn-danger btn-sm pull-left" onclick="btnDelete()"><@spring.message code='common.btn.delete' /></button>
                        <button type="button" class="btn btn-info btn-sm pull-right" onclick="btnSave()"><@spring.message code='common.btn.save' /></button>
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
<script src="/static/halo-backend/js/adminlte.min.js"></script>
<script src="/static/halo-backend/plugins/layer/layer.js"></script>
<script src="/static/halo-backend/js/halo.min.js"></script>
<script>
    var halo = new $.halo();
    function btnDelete() {
        layer.msg('<@spring.message code="common.text.define-delete" />', {
            time: 0
            ,btn: ['<@spring.message code="common.btn.delete" />', '<@spring.message code="common.btn.cancel" />']
            ,yes: function(index){
                layer.close(index);
                $.get('/admin/page/gallery/remove',{'galleryId' : ${gallery.galleryId?c}},function (data) {
                    if(data.code === 1){
                        halo.showMsgAndParentRedirect(data.msg,'success',1000,"/admin/page/galleries");
                    }else{
                        halo.showMsg(data.msg,'error',2000);
                    }
                },'JSON');
            }
        });
    }
    function btnSave() {
        $('#galleryForm').submit();
        parent.location.reload();
    }
</script>
</html>
