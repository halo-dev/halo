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
                        <a href="#about" data-toggle="tab">关于</a>
                    </li>
                </ul>
                <div class="tab-content">
                    <!-- 社交资料 -->
                    <div class="tab-pane active" id="general">
                        <form method="post" class="form-horizontal" id="storyGeneralOptions">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="storyGeneralBackground" class="col-sm-4 control-label">首页背景图：</label>
                                    <div class="col-sm-8">
                                        <div class="input-group">
                                            <input type="text" class="form-control" id="storyGeneralBackground" name="story_general_background" value="${options.story_general_background?if_exists}" >
                                            <span class="input-group-btn">
                                                <button class="btn btn-default btn-flat" type="button" onclick="openAttach('storyGeneralBackground')">选择</button>
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label for="storyGeneralTitle" class="col-sm-4 control-label">首页标题：</label>
                                    <div class="col-sm-8">
                                        <textarea class="form-control" rows="6" id="storyGeneralTitle" name="story_general_title" style="resize: none">${options.story_general_title?default('<span class="b">Y</span>
<span class="b">U</span>
<a href="/">
<span class="w">M</span>
</a>
<span class="b">O</span>
<span class="b">E</span>')}</textarea>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label for="storyGeneralFavicon" class="col-sm-4 control-label">Favicon：</label>
                                    <div class="col-sm-8">
                                        <div class="input-group">
                                            <input type="text" class="form-control" id="storyGeneralFavicon" name="story_general_favicon" value="${options.story_general_favicon?if_exists}" >
                                            <span class="input-group-btn">
                                                <button class="btn btn-default btn-flat" type="button" onclick="openAttach('storyGeneralFavicon')">选择</button>
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label for="storyGeneralLocale" class="col-sm-4 control-label">日期显示中/英文：</label>
                                    <div class="col-sm-8">
                                        <label class="radio-inline">
                                            <input type="radio" name="story_general_locale" id="storyGeneralLocale" value="en" ${((options.story_general_locale?default('en'))=='en')?string('checked','')}> 英文
                                        </label>
                                        <label class="radio-inline">
                                            <input type="radio" name="story_general_locale" id="storyGeneralLocale" value="zh" ${((options.story_general_locale?if_exists)=='zh')?string('checked','')}> 中文
                                        </label>
                                    </div>
                                </div>
                            </div>
                            <div class="box-footer">
                                <button type="button" class="btn btn-primary btn-sm pull-right" onclick="saveThemeOptions('storyGeneralOptions')">保存设置</button>
                            </div>
                        </form>
                    </div>
                    <!-- 关于该主题 -->
                    <div class="tab-pane" id="about">
                        <div class="box box-widget widget-user-2">
                            <div class="widget-user-header bg-blue">
                                <div class="widget-user-image">
                                    <img class="img-circle" src="https://avatars2.githubusercontent.com/u/9015538?s=460&v=4" alt="User Avatar">
                                </div>
                                <h3 class="widget-user-username">Trii Hsia</h3>
                                <h5 class="widget-user-desc">人生本是梦中精彩的绽放</h5>
                            </div>
                            <div class="box-footer no-padding">
                                <ul class="nav nav-stacked">
                                    <li><a target="_blank" href="https://yumoe.com/">作者主页</a></li>
                                    <li><a target="_blank" href="https://github.com/txperl/Story-for-Typecho">原主题地址</a></li>
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
