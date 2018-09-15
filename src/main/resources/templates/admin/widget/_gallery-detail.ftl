<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="/static/plugins/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="/static/plugins/toast/css/jquery.toast.min.css">
    <link rel="stylesheet" href="/static/css/AdminLTE.min.css">
    <style>
        .attachDesc,.attachImg{padding-top:15px;padding-bottom:15px}
        .form-horizontal .control-label{text-align:left}
    </style>
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <div class="col-lg-6 attachImg">
            <img src="${gallery.galleryUrl?if_exists}" style="width: 100%;">
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
                                <input type="text" class="form-control" id="galleryDesc" name="galleryDesc" value="${gallery.galleryDesc?if_exists}" >
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="galleryDate" class="col-sm-2 control-label"><@spring.message code='admin.pages.galleries.form.gallery-date' /></label>
                            <div class="col-sm-10">
                                <input type="date" class="form-control" id="galleryDate" name="galleryDate" value="${gallery.galleryDate?if_exists}">
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="galleryLocation" class="col-sm-2 control-label"><@spring.message code='admin.pages.galleries.form.gallery-location' /></label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="galleryLocation" name="galleryLocation" value="${gallery.galleryLocation?if_exists}" >
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
                        <button type="button" class="btn btn-danger btn-sm pull-left" onclick="btn_delete()"><@spring.message code='common.btn.delete' /></button>
                        <button type="button" class="btn btn-info btn-sm pull-right" onclick="btn_save()"><@spring.message code='common.btn.save' /></button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/static/plugins/jquery/jquery.min.js"></script>
<script src="/static/plugins/bootstrap/js/bootstrap.min.js"></script>
<script src="/static/plugins/toast/js/jquery.toast.min.js"></script>
<script src="/static/js/adminlte.min.js"></script>
<script src="/static/plugins/layer/layer.js"></script>
<script src="/static/js/app.js"></script>
<script>
    function btn_delete() {
        layer.msg('<@spring.message code="common.text.define-delete" />', {
            time: 0
            ,btn: ['<@spring.message code="common.btn.delete" />', '<@spring.message code="common.btn.cancel" />']
            ,yes: function(index){
                layer.close(index);
                $.ajax({
                    type: 'GET',
                    url: '/admin/page/gallery/remove',
                    async: false,
                    data:{
                        galleryId : ${gallery.galleryId?c}
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
                                    parent.location.reload();
                                }
                            });
                        }else{
                            showMsg(data.msg,"error",2000);
                        }
                    }
                });
            }
        });
    }
    function btn_save() {
        $('#galleryForm').submit();
        parent.location.reload();
    }
</script>
</html>
