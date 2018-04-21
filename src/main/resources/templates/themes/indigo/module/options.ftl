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
                        <a href="#general" data-toggle="tab">基本设置</a>
                    </li>
                    <li>
                        <a href="#style" data-toggle="tab">样式设置</a>
                    </li>
                    <li>
                        <a href="#about" data-toggle="tab">关于</a>
                    </li>
                </ul>
                <div class="tab-content">
                    <!-- 社交资料 -->
                    <div class="tab-pane active" id="general">
                        <form method="post" class="form-horizontal" id="indigoGeneralOptions">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="indigoGeneralBrand" class="col-sm-4 control-label">头像背景图：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="indigoGeneralBrand" name="indigo_general_brand" value="${options.indigo_general_brand?default("indigo/source/img/brand.jpg")}" >
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="indigoGeneralFavicon" class="col-sm-4 control-label">Favicon：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="indigoGeneralFavicon" name="indigo_general_favicon" value="${options.indigo_general_favicon?if_exists}" >
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="anatoleSnsInstagram" class="col-sm-4 control-label">Instagram：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="anatoleSnsInstagram" name="theme_anatole_sns_instagram" value="${options.theme_anatole_sns_instagram?if_exists}" >
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="anatoleSnsDribbble" class="col-sm-4 control-label">Dribbble：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="anatoleSnsDribbble" name="theme_anatole_sns_dribbble" value="${options.theme_anatole_sns_dribbble?if_exists}" >
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="anatoleSnsWeibo" class="col-sm-4 control-label">Weibo：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="anatoleSnsWeibo" name="theme_anatole_sns_weibo" value="${options.theme_anatole_sns_weibo?if_exists}" >
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="anatoleSnsEmail" class="col-sm-4 control-label">Email：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="anatoleSnsEmail" name="theme_anatole_sns_email" value="${options.theme_anatole_sns_email?if_exists}" >
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="anatoleSnsGithub" class="col-sm-4 control-label">Github：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="anatoleSnsGithub" name="theme_anatole_sns_github" value="${options.theme_anatole_sns_github?if_exists}" >
                                    </div>
                                </div>
                            </div>
                            <div class="box-footer">
                                <button type="button" class="btn btn-primary btn-sm pull-right" onclick="saveThemeOptions('indigoGeneralOptions')">保存设置</button>
                            </div>
                        </form>
                    </div>
                    <div class="tab-pane" id="style">
                        <form method="post" class="form-horizontal" id="indigoStyleOptions">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="indigoStyleBarColor" class="col-sm-4 control-label">浏览器状态栏颜色：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="indigoStyleBarColor" name="indigo_style_barColor" value="${options.indigo_style_barColor?default("#3F51B5")}" >
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="indigoStyleTagsTitle" class="col-sm-4 control-label">标签页面标题：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="indigoStyleTagsTitle" name="indigo_style_tagsTitle" value="${options.indigo_style_tagsTitle?default("Tags")}" >
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="indigoStyleArchivesTitle" class="col-sm-4 control-label">归档页面标题：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="indigoStyleArchivesTitle" name="indigoStyle_archivesTitle" value="${options.indigoStyle_archivesTitle?default("Archives")}" >
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="indigoStyleCategoriesTitle" class="col-sm-4 control-label">归档页面标题：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="indigoStyleCategoriesTitle" name="indigoStyle_categoriesTitle" value="${options.indigoStyle_categoriesTitle?default("Categories")}" >
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="indigoStyleShowLastUpdated" class="col-sm-4 control-label">显示文章最后更新时间：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="indigoStyleShowLastUpdated" name="indigoStyle_showLastUpdated" value="${options.indigoStyle_showLastUpdated?default("Categories")}" >
                                    </div>
                                </div>

                            </div>
                            <div class="box-footer">
                                <button type="button" class="btn btn-primary btn-sm pull-right" onclick="saveThemeOptions('indigoStyleOptions')">保存设置</button>
                            </div>
                        </form>
                    </div>
                    <!-- 关于该主题 -->
                    <div class="tab-pane" id="about">
                        <div class="box box-widget widget-user-2">
                            <div class="widget-user-header bg-blue">
                                <div class="widget-user-image">
                                    <img class="img-circle" src="/anatole/source/images/logo@2x.png" alt="User Avatar">
                                </div>
                                <h3 class="widget-user-username">CAICAI</h3>
                                <h5 class="widget-user-desc">A other farbox theme</h5>
                            </div>
                            <div class="box-footer no-padding">
                                <ul class="nav nav-stacked">
                                    <li><a target="_blank" href="https://www.caicai.me/">作者主页</a></li>
                                    <li><a target="_blank" href="https://github.com/hi-caicai/farbox-theme-Anatole">原主题地址</a></li>
                                </ul>
                            </div>
                        </div>
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