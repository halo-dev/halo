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
            <img src="${attachment.attachPath?if_exists}" style="width: 100%;">
        </div>
        <div class="col-lg-6 attachDesc">
            <div class="box box-solid">
                <div class="box-header with-border">
                    <h3 class="box-title">详细信息</h3>
                </div>
                <form class="form-horizontal">
                    <div class="box-body">
                        <div class="form-group">
                            <label for="attachName" class="col-sm-2 control-label">附件名：</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="attachName" value="${attachment.attachName?if_exists}">
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="attachType" class="col-sm-2 control-label">附件类型：</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="attachType" value="${attachment.attachType?if_exists}" disabled>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="attachPath" class="col-sm-2 control-label">附件路径：</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="attachPath" value="${attachment.attachPath?if_exists}" disabled>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="attachCreated" class="col-sm-2 control-label">上传时间：</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="attachCreated" value="${attachment.attachCreated?if_exists}" disabled>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="attachStorage" class="col-sm-2 control-label">附件大小：</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="attachStorage" disabled>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="attachSize" class="col-sm-2 control-label">图片尺寸：</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control" id="attachSize" disabled>
                            </div>
                        </div>
                    </div>
                    <div class="box-footer">
                        <button type="button" class="btn btn-danger btn-sm pull-left" onclick="btn_delete()">永久删除</button>
                        <button type="button" class="btn btn-info btn-sm pull-right btn-copy" data-clipboard-text="${attachment.attachPath}">复制链接</button>
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
<script src="/static/plugins/clipboard/clipboard.min.js"></script>
<script src="/static/js/adminlte.min.js"></script>
<script src="/static/plugins/layer/layer.js"></script>
<script src="/static/js/app.js"></script>
<script>
    function btn_delete() {
        layer.msg('你确定要删除？', {
            time: 0
            ,btn: ['删除', '取消']
            ,yes: function(index){
                layer.close(index);
                $.ajax({
                    type: 'GET',
                    url: '/admin/attachments/remove',
                    async: false,
                    data:{
                        attachId : ${attachment.attachId?c}
                    },
                    success: function (data) {
                        if(data==true){
                            $.toast({
                                text: "删除成功！",
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
                                    parent.location.reload();
                                }
                            });
                        }else{
                            showMsg("删除失败","error",2000);
                        }
                    }
                });
            }
        });
    }
    $(document).ready(function(){
        var clipboard = new Clipboard('.btn-copy');
        var img = document.getElementsByTagName("img")[0];
        //获取文件的大小和尺寸
        var width = img.naturalWidth;
        var height = img.naturalHeight;
        var image = new Image();
        image.src = img.src;
        $('#attachSize').val(width+'x'+height);
        $('#attachStorage').val('256k');
    });
    $('.btn-copy').click(function () {
        showMsg("复制成功","success",1000)
    })
</script>
</html>