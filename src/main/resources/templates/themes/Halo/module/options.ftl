<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="/static/plugins/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="/static/plugins/toast/css/jquery.toast.min.css">
    <link rel="stylesheet" href="/static/plugins/colorpicker/css/bootstrap-colorpicker.min.css">
    <link rel="stylesheet" href="/static/css/AdminLTE.min.css">
    <style>
        .themeSetting,.themeImg{
            padding-top: 15px;
            padding-bottom: 15px;
        }
        .form-horizontal .control-label{
            text-align: left;
        }
    </style>
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <div class="col-lg-6 themeImg">
            <img src="/${themeDir}/screenshot.png" style="width: 100%;">
        </div>
        <div class="col-md-6 themeSetting">
            <div class="nav-tabs-custom">
                <ul class="nav nav-tabs">
                    <li class="active">
                        <a href="#general" data-toggle="tab">基础设置</a>
                    </li>
                    <li>
                        <a href="#style" data-toggle="tab">样式设置</a>
                    </li>
                    <li>
                        <a href="#sns" data-toggle="tab">社交资料</a>
                    </li>
                </ul>
                <div class="tab-content">
                    <!-- 基础设置 -->
                    <div class="tab-pane active" id="general">
                        <form method="post" class="form-horizontal" id="haloGeneralOptions">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="haloPjax" class="col-sm-4 control-label">启用pjax：</label>
                                    <div class="col-sm-8">
                                        <label class="radio-inline">
                                            <input type="radio" name="theme_halo_pjax" id="haloPjax" value="true" ${((options.theme_halo_pjax?if_exists)=='true')?string('checked','')}> 开启
                                        </label>
                                        <label class="radio-inline">
                                            <input type="radio" name="theme_halo_pjax" id="haloPjax" value="false" ${((options.theme_halo_pjax?if_exists)=='false')?string('checked','')}> 关闭
                                        </label>
                                    </div>
                                </div>
                            </div>
                            <div class="box-footer">
                                <button type="button" class="btn btn-primary btn-sm pull-right" onclick="saveThemeOptions('haloGeneralOptions')">保存设置</button>
                            </div>
                        </form>
                    </div>

                    <!-- 样式设置 -->
                    <div class="tab-pane" id="style">
                        <form method="post" class="form-horizontal" id="haloStyleOptions">
                            <div class="box-body">

                            </div>
                            <div class="box-footer">
                                <button type="button" class="btn btn-primary btn-sm pull-right" onclick="saveThemeOptions('haloStyleOptions')">保存设置</button>
                            </div>
                        </form>
                    </div>

                    <!-- 社交资料 -->
                    <div class="tab-pane" id="sns">
                        <form method="post" class="form-horizontal" id="haloSnsOptions">
                            <div class="box-body">

                            </div>
                            <div class="box-footer">
                                <button type="button" class="btn btn-primary btn-sm pull-right" onclick="saveThemeOptions('haloSnsOptions')">保存设置</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/static/plugins/jquery/jquery.min.js"></script>
<script src="/static/plugins/bootstrap/js/bootstrap.min.js"></script>
<script src="/static/plugins/toast/js/jquery.toast.min.js"></script>
<script src="/static/plugins/layer/layer.js"></script>
<script src="/static/plugins/colorpicker/js/bootstrap-colorpicker.min.js"></script>
<script src="/static/js/adminlte.min.js"></script>
<script src="/static/js/app.js"></script>
<script>
    function saveThemeOptions(option) {
        var param = $('#'+option).serialize();
        $.ajax({
            type: 'post',
            url: '/admin/option/save',
            data: param,
            success: function (data) {
                showMsg("保存成功！","success",1000);
            }
        });
    }
</script>
</html>