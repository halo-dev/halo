<#macro head>
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
<script src="/static/halo-common/jquery/jquery.min.js"></script>
<script src="/static/halo-backend/plugins/bootstrap/js/bootstrap.min.js"></script>
<script src="/static/halo-backend/plugins/toast/js/jquery.toast.min.js"></script>
<script src="/static/halo-backend/plugins/layer/layer.js"></script>
<script src="/static/halo-backend/js/halo.min.js"></script>
<#nested />
<script>
    var halo = new $.halo();
    var heading = "<@spring.message code='common.text.tips' />";
    /**
     * 保存设置选项
     * @param option option
     */
    function saveThemeOptions(option) {
        var param = $('#'+option).serialize();
        $.post('/admin/option/save',param,function (data) {
            if(data.code === 1){
                halo.showMsg(data.msg, "success", 1000);
            }else{
                halo.showMsg(data.msg, "error", 1000);
            }
        },'JSON');
    }

    /**
     * 所有附件
     * @param id id
     */
    function openAttach(id) {
        layer.open({
            type: 2,
            title: '<@spring.message code="common.js.all-attachment" />',
            shadeClose: true,
            shade: 0.5,
            area: ['90%', '90%'],
            content: '/admin/attachments/select?id='+id,
            scrollbar: false
        });
    }

    /**
     * 更新主题
     */
    function updateTheme(theme, e) {
        $(e).button('loading');
        $.get('/admin/themes/pull',{'themeName': theme},function (data) {
            if (data.code === 1) {
                halo.showMsgAndParentRedirect(data.msg, 'success', 1000, '/admin/themes');
            } else {
                halo.showMsg(data.msg, 'error', 2000);
                $(e).button('reset');
            }
        },'JSON');
    }
</script>
</html>
</#macro>
