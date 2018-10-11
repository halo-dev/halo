<#macro head>
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
        .themeSetting, .themeImg {
            padding-top: 15px;
            padding-bottom: 15px;
        }

        .form-horizontal .control-label {
            text-align: left;
        }
    </style>
</head>
<body>
</#macro>

<#macro import_js>
</body>
<script src="/static/plugins/jquery/jquery.min.js"></script>
<script src="/static/plugins/bootstrap/js/bootstrap.min.js"></script>
<script src="/static/plugins/toast/js/jquery.toast.min.js"></script>
<script src="/static/plugins/layer/layer.js"></script>
<script src="/static/js/halo.js"></script>
<#nested />
<script>
    function saveThemeOptions(option) {
        var param = $('#'+option).serialize();
        $.ajax({
            type: 'post',
            url: '/admin/option/save',
            data: param,
            success: function (data) {
                if(data.code==1){
                    showMsg(data.msg,"success",1000);
                }else{
                    showMsg(data.msg,"error",1000);
                }
            }
        });
    }
    function openAttach(id) {
        layer.open({
            type: 2,
            title: '所有附件',
            shadeClose: true,
            shade: 0.5,
            area: ['90%', '90%'],
            content: '/admin/attachments/select?id='+id,
            scrollbar: false
        });
    }
</script>
</html>
</#macro>
