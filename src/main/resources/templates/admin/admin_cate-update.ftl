<#compress >
<#include "module/_macro.ftl">
<@head title="Halo后台管理-修改分类">
</@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <link rel="stylesheet" href="/static/plugins/toast/css/jquery.toast.min.css">
        <style type="text/css" rel="stylesheet">
            .form-horizontal .control-label{text-align: left;}
        </style>
        <section class="content-header">
            <h1>分类目录<small></small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="/admin"><i class="fa fa-dashboard"></i> 首页</a></li>
                <li><a href="/admin/category"><i class="fa fa-dashboard"></i> 分类目录</a></li>
                <li class="active">修改</li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div class="row">
                <div class="col-md-6">
                    <div class="box box-solid">
                        <div class="box-header with-border"><h3 class="box-title">修改分类目录</h3></div>
                        <form action="/admin/category/update" method="post" class="form-horizontal" onsubmit="return checkCate()">
                            <input type="hidden" name="cateId" value="${category.cateId}">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="cateName" class="col-sm-3 control-label">名称：</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="cateName" name="cateName" value="${category.cateName}">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="cateUrl" class="col-sm-3 control-label">路径名称：</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="cateUrl" name="cateUrl" value="${category.cateUrl}">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="cateDesc" class="col-sm-3 control-label">描述：</label>
                                    <div class="col-sm-9">
                                        <textarea class="form-control" rows="3" id="cateDesc" name="cateDesc" style="resize: none">${category.cateDesc}</textarea>
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
                var name = $('#cateName').val();
                var url = $('#cateUrl').val();
                var desc = $('#cateDesc').val();
                var result = true;
                if(name==""||url==""||desc==""){
                    showMsg("请输入完整信息！","info",2000);
                    result = false;
                }
                return result;
            }
        </script>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>
</#compress>