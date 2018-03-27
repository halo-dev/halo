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
            #btnNewPage{margin-left:4px;padding:3px 6px;position:relative;top:-4px;border:1px solid #ccc;border-radius:2px;background:#fff;text-shadow:none;font-weight:600;font-size:12px;line-height:normal;color:#3c8dbc;cursor:pointer;transition:all .2s ease-in-out}
            #btnNewPage:hover{background:#3c8dbc;color:#fff}
        </style>
        <section class="content-header">
            <h1 style="display: inline-block;">页面<small></small></h1>
            <a id="btnNewPage" href="/admin/posts/new">
                新建页面
            </a>
            <ol class="breadcrumb">
                <li>
                    <a data-pjax="true" href="/admin"><i class="fa fa-dashboard"></i> 首页</a>
                </li>
                <li><a data-pjax="true" href="#">页面</a></li>
                <li class="active">所有页面</li>
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
                                            <td>/links</td>
                                            <td>
                                                <a href="/links" class="btn btn-info btn-sm btn-flat" target="_blank">预览</a>
                                                <a href="/admin/page/links" class="btn btn-primary btn-sm btn-flat">配置</a>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>图库页面</td>
                                            <td>/about</td>
                                            <td>
                                                <a href="/gallery" class="btn btn-info btn-sm btn-flat" target="_blank">预览</a>
                                                <a href="/admin/page/gallery" class="btn btn-primary btn-sm btn-flat">配置</a>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>关于页面</td>
                                            <td>/about</td>
                                            <td>
                                                <a href="/about" class="btn btn-info btn-sm btn-flat" target="_blank">预览</a>
                                                <a href="/admin/page/about" class="btn btn-primary btn-sm btn-flat">配置</a>
                                            </td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                            <div class="tab-pane" id="pages">
                                <div class="box-body table-responsive">
                                    <table class="table table-bordered table-hover">
                                        <thead>
                                        <tr>
                                            <th>标题</th>
                                            <th>路径</th>
                                            <th>日期</th>
                                            <th>操作</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <tr>
                                            <td>友情链接</td>
                                            <td>/link</td>
                                            <th>日期</th>
                                            <td>
                                                <a href="/links" class="btn btn-info btn-sm btn-flat" target="_blank">预览</a>
                                                <a href="/admin/page/links" class="btn btn-primary btn-sm btn-flat">配置</a>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>关于页面</td>
                                            <td>/about</td>
                                            <th>日期</th>
                                            <td>
                                                <a href="#" class="btn btn-info btn-sm btn-flat" target="_blank">预览</a>
                                                <a href="/admin/page/about" class="btn btn-primary btn-sm btn-flat">配置</a>
                                            </td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>
</#compress>