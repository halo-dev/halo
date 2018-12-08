<#import "/common/macro/theme_option_marco.ftl" as option>
<@option.head />
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
                        <form method="post" class="form-horizontal" id="yummyGeneralOptions">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="yummyGeneralLocation" class="col-sm-4 control-label">地理位置：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="yummyGeneralLocation" name="yummy_general_location" value="${options.yummy_general_location?if_exists}" >
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="yummyGeneralCompany" class="col-sm-4 control-label">公司名称：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="yummyGeneralCompany" name="yummy_general_company" value="${options.yummy_general_company?if_exists}" >
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="yummyGeneralCompanyUrl" class="col-sm-4 control-label">公司网址：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="yummyGeneralCompanyUrl" name="yummy_general_company_url" value="${options.yummy_general_company_url?if_exists}" >
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="yummyGeneralGithubUrl" class="col-sm-4 control-label">Github地址：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="yummyGeneralGithubUrl" name="yummy_general_github_url" value="${options.yummy_general_github_url?if_exists}" >
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="yummyGeneralGithubUsername" class="col-sm-4 control-label">Github用户名：</label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="yummyGeneralGithubUsername" name="yummy_general_github_username" value="${options.yummy_general_github_username?if_exists}" >
                                    </div>
                                </div>
                            </div>
                            <div class="box-footer">
                                <button type="button" class="btn btn-primary btn-sm pull-right" onclick="saveThemeOptions('yummyGeneralOptions')">保存设置</button>
                            </div>
                        </form>
                    </div>
                    <!-- 关于该主题 -->
                    <div class="tab-pane" id="about">
                        <div class="box box-widget widget-user-2">
                            <div class="widget-user-header bg-blue">
                                <div class="widget-user-image">
                                    <img class="img-circle" src="https://avatars2.githubusercontent.com/u/2586841?s=460&v=4" alt="User Avatar">
                                </div>
                                <h3 class="widget-user-username">DONGChuan</h3>
                                <h5 class="widget-user-desc">A Simple, Bootstrap Based Theme</h5>
                            </div>
                            <div class="box-footer no-padding">
                                <ul class="nav nav-stacked">
                                    <li><a target="_blank" href="http://dongchuan.github.io/">作者主页</a></li>
                                    <li><a target="_blank" href="https://github.com/DONGChuan/Yummy-Jekyll">原主题地址</a></li>
                                </ul>
                            </div>
                            <#if hasUpdate>
                                <div class="box-footer">
                                    <button type="button" class="btn btn-warning btn-sm pull-right" data-loading-text="更新中..." onclick="updateTheme('${themeDir}',this)">更新主题</button>
                                </div>
                            </#if>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<@option.import_js />
