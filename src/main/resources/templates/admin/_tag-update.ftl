<#compress >
<#include "module/_macro.ftl">
<@head title="Halo后台管理-修改标签">
</@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <link rel="stylesheet" href="/static/plugins/toast/css/jquery.toast.min.css">
        <style type="text/css" rel="stylesheet">
            .form-horizontal .control-label{
                text-align: left;
            }
        </style>
        <section class="content-header">
            <h1>标签<small></small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="/admin"><i class="fa fa-dashboard"></i> 首页</a></li>
                <li><a href="/admin/category"><i class="fa fa-dashboard"></i> 标签</a></li>
                <li class="active">修改</li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div class="row">
                <div class="col-md-6">
                    <div class="box box-solid">
                        <div class="box-header with-border"><h3 class="box-title">修改标签</h3></div>
                        <form action="/admin/tag/update" method="post" class="form-horizontal" onsubmit="return checkCate()">
                            <input type="hidden" name="tagId" value="${tag.tagId}">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="tagName" class="col-sm-3 control-label">名称：</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="tagName" name="tagName" value="${tag.tagName}">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="tagUrl" class="col-sm-3 control-label">路径名称：</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="tagUrl" name="tagUrl" value="${tag.tagUrl}">
                                    </div>
                                </div>
                            </div>
                            <div class="box-footer">
                                <button class="btn btn-default" onclick="window.history.back()">返回</button>
                                <button type="submit" class="btn btn-primary pull-right">保存</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </section>
        <script src="/static/plugins/toast/js/jquery.toast.min.js"></script>
        <script>
            function checkCate() {
                var name = $('#tagName').val();
                var url = $('#tagUrl').val();
                var result = true;
                if(name==""||url==""){
                    showMsg("请输入完整信息！","info",2000);
                    result = false;
                }
                return result;
            }
        </script>
    </div>
    <#include "module/_footer.ftl">
    <div class="control-sidebar-bg"></div>
</div>
<@footer></@footer>
</#compress>