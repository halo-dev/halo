<#compress >
<#include "module/_macro.ftl">
<@head title="Halo后台管理-友情链接修改">
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
            <h1>友情链接<small></small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="/admin"><i class="fa fa-dashboard"></i> 首页</a></li>
                <li><a href="/admin/page">页面</a></li>
                <li><a href="/admin/page/links">友情链接</a></li>
                <li class="active">修改</li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div class="row">
                <div class="col-md-6">
                    <div class="box box-solid">
                        <div class="box-header with-border"><h3 class="box-title">修改友情链接</h3></div>
                        <form action="/admin/page/links/update" method="post" class="form-horizontal" onsubmit="return isNull()">
                            <input type="hidden" name="linkId" value="${link.linkId}">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="cateName" class="col-sm-3 control-label">网站名称：</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="linkName" name="linkName" value="${link.linkName}">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="cateUrl" class="col-sm-3 control-label">网址：</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="linkUrl" name="linkUrl" value="${link.linkUrl}">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="cateUrl" class="col-sm-3 control-label">LOGO：</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="linkPic" name="linkPic" value="${link.linkPic}">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="cateDesc" class="col-sm-3 control-label">描述：</label>
                                    <div class="col-sm-9">
                                        <textarea class="form-control" rows="3" id="linkDesc" name="linkDesc" style="resize: none">${link.linkDesc}</textarea>
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
            function isNull() {
                var name = $('#linkName').val();
                var url = $('#linkUrl').val();
                var pic = $('#linkPic').val();
                var desc = $('#linkDesc').val();
                if(name==""||url==""||pic==""){
                    showMsg("请输入完整信息！","info",2000);
                    return false;
                }
            }
        </script>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>
</#compress>