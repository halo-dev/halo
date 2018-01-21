<#compress >
<#include "module/_macro.ftl">
<@head title="Halo后台管理-页面">
</@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <style type="text/css" rel="stylesheet">
            .form-horizontal .control-label{
                text-align: left;
            }
            .nav-tabs-custom > .nav-tabs > li.active {
                border-top-color: #d2d6de;
            }
        </style>
        <section class="content-header">
            <h1>页面<small></small></h1>
            <ol class="breadcrumb">
                <li>
                    <a href="/admin"><i class="fa fa-dashboard"></i> 首页</a>
                </li>
                <li class="active">页面</li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div class="row">
                <div class="col-md-12">
                    <div class="nav-tabs-custom">
                        <ul class="nav nav-tabs">
                            <li class="active">
                                <a href="#internal" data-toggle="tab">内置页面</a>
                            </li>
                            <li>
                                <a href="#pages" data-toggle="tab">自定义页面</a>
                            </li>
                        </ul>
                        <div class="tab-content">
                            <div class="tab-pane active" id="internal">
                                <div class="box-body table-responsive">
                                    <table class="table table-bordered table-hover">
                                        <thead>
                                        <tr>
                                            <th>标题</th>
                                            <th>路径</th>
                                            <th>操作</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <tr>
                                            <td>友情链接</td>
                                            <td>/link</td>
                                            <td>
                                                <a href="/links" class="btn btn-info btn-sm" target="_blank">预览</a>
                                                <a href="/admin/page/links" class="btn btn-primary btn-sm">配置</a>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>关于页面</td>
                                            <td>/about</td>
                                            <td>
                                                <a href="#" class="btn btn-info btn-sm" target="_blank">预览</a>
                                                <a href="/admin/page/about" class="btn btn-primary btn-sm">配置</a>
                                            </td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                            <div class="tab-pane" id="pages">
                                <form method="post" class="form-horizontal" id="seoOptions">
                                    <div class="box-body">
                                        <div class="form-group">
                                            <label for="keywords" class="col-sm-2 control-label">QQ：</label>
                                            <div class="col-sm-4">
                                                <input type="text" class="form-control" id="keywords" name="seo_keywords" value="">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="desc" class="col-sm-2 control-label">微信：</label>
                                            <div class="col-sm-4">
                                                <input type="text" class="form-control" id="desc" name="seo_desc" value="">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="siteMap" class="col-sm-2 control-label">站点地图：</label>
                                            <div class="col-sm-4">
                                                <input type="text" class="form-control" id="siteMap" name="seo_site_map" value="">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="baiduToken" class="col-sm-2 control-label">百度推送token：</label>
                                            <div class="col-sm-4">
                                                <input type="text" class="form-control" id="baiduToken" name="seo_baidu_token" value="">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="seoRobots" class="col-sm-2 control-label">robots.txt：</label>
                                            <div class="col-sm-4">
                                                <textarea class="form-control" rows="5" id="seoRobots" name="seo_robots" style="resize: none"></textarea>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="box-footer">
                                        <button type="button" class="btn btn-primary btn-sm" onclick="saveOptions('seoOptions')">保存</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </div>
    <#include "module/_footer.ftl">
    <div class="control-sidebar-bg"></div>
</div>
<@footer></@footer>
</#compress>