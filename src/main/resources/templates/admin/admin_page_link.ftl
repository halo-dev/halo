<#compress >
<#include "module/_macro.ftl">
<@head title="${options.blog_title} | 后台管理：友情链接">
</@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <link rel="stylesheet" href="/static/plugins/toast/css/jquery.toast.min.css">
        <section class="content-header">
            <h1>友情链接<small></small></h1>
            <ol class="breadcrumb">
                <li><a data-pjax="true" href="/admin"><i class="fa fa-dashboard"></i>首页</a></li>
                <li><a data-pjax="true" href="/admin/page">页面</a></li>
                <li class="active">友情链接</li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div class="row">
                <div class="col-md-5">
                    <div class="box box-primary">
                        <div class="box-header with-border"><h3 class="box-title">${statusName}友情链接</h3></div>
                        <#if updateLink??>
                            <form action="/admin/page/links/save" method="post" role="form" onsubmit="return isNull()">
                                <input type="hidden" name="linkId" value="${updateLink.linkId}">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label for="exampleInputEmail1">网站名称</label>
                                        <input type="text" class="form-control" id="linkName" name="linkName" value="${updateLink.linkName}">
                                        <small>好友的网站名称</small>
                                    </div>
                                    <div class="form-group">
                                        <label for="exampleInputPassword1">网址</label>
                                        <input type="url" class="form-control" id="linkUrl" name="linkUrl" value="${updateLink.linkUrl}">
                                        <small>*需要加上http://或https://</small>
                                    </div>
                                    <div class="form-group">
                                        <label for="exampleInputPassword1">LOGO</label>
                                        <input type="text" class="form-control" id="linkPic" name="linkPic" value="${updateLink.linkPic}">
                                        <small>*LOGO链接地址，需要加上http://或https://，在部分主题可显示</small>
                                    </div>
                                    <div class="form-group">
                                        <label for="exampleInputPassword1">描述</label>
                                        <textarea class="form-control" rows="3" id="linkDesc" name="linkDesc" style="resize: none">${updateLink.linkDesc}</textarea>
                                        <small>*网站的描述，部分主题可显示</small>
                                    </div>
                                </div>
                                <div class="box-footer">
                                    <button type="submit" class="btn btn-primary btn-sm ">确定${statusName}</button>
                                    <a data-pjax="true" href="/admin/page/links" class="btn btn-info btn-sm ">返回添加</a>
                                </div>
                            </form>
                        <#else>
                            <form action="/admin/page/links/save" method="post" role="form" onsubmit="return isNull()">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label for="exampleInputEmail1">网站名称</label>
                                        <input type="text" class="form-control" id="linkName" name="linkName" placeholder="">
                                        <small>好友的网站名称</small>
                                    </div>
                                    <div class="form-group">
                                        <label for="exampleInputPassword1">网址</label>
                                        <input type="text" class="form-control" id="linkUrl" name="linkUrl" placeholder="">
                                        <small>*需要加上http://或https://</small>
                                    </div>
                                    <div class="form-group">
                                        <label for="exampleInputPassword1">LOGO</label>
                                        <input type="text" class="form-control" id="linkPic" name="linkPic" placeholder="">
                                        <small>*LOGO链接地址，需要加上http://或https://，在部分主题可显示</small>
                                    </div>
                                    <div class="form-group">
                                        <label for="exampleInputPassword1">描述</label>
                                        <textarea class="form-control" rows="3" id="linkDesc" name="linkDesc" style="resize: none"></textarea>
                                        <small>*网站的描述，部分主题可显示</small>
                                    </div>
                                </div>
                                <div class="box-footer">
                                    <button type="submit" class="btn btn-primary btn-sm ">确定${statusName}</button>
                                </div>
                            </form>
                        </#if>
                    </div>
                </div>
                <div class="col-md-7">
                    <div class="box box-primary">
                        <div class="box-header with-border"><h3 class="box-title">所有友情链接</h3></div>
                        <div class="box-body table-responsive">
                            <table class="table table-hover">
                                <thead>
                                <tr><th>名称</th><th>网址</th><th>描述</th><th>操作</th></tr>
                                </thead>
                                <tbody>
                                    <#list links as link>
                                    <tr>
                                        <td>${link.linkName}</td>
                                        <td>${link.linkUrl}</td>
                                        <td>${link.linkDesc}</td>
                                        <td>
                                            <#if updateLink?? && updateLink.linkId==link.linkId>
                                                <a class="btn btn-primary btn-xs" href="#" disabled>正在修改</a>
                                            <#else >
                                            <a data-pjax="true" class="btn btn-primary btn-xs" href="/admin/page/links/edit?linkId=${link.linkId}">修改</a>
                                            </#if>
                                            <button class="btn btn-danger btn-xs" onclick="modelShow('/admin/page/links/remove?linkId=${link.linkId}')">删除</>
                                        </td>
                                    </tr>
                                    </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        <!-- 删除确认弹出层 -->
        <div class="modal fade" id="removeLinkModal">
            <div class="modal-dialog">
                <div class="modal-content message_align">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span></button>
                        <h4 class="modal-title">提示信息</h4>
                    </div>
                    <div class="modal-body"><p>您确认要删除吗？</p></div>
                    <div class="modal-footer">
                        <input type="hidden" id="url"/>
                        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                        <a onclick="removeIt()" class="btn btn-danger" data-dismiss="modal">确定</a>
                    </div>
                </div>
            </div>
        </div>
        <script src="/static/plugins/toast/js/jquery.toast.min.js"></script>
        <script>
            function modelShow(url) {
                $('#url').val(url);
                $('#removeLinkModal').modal();
            }
            function removeIt(){
                var url=$.trim($("#url").val());
                window.location.href=url;
            }
            function isNull() {
                var name = $('#linkName').val();
                var url = $('#linkUrl').val();
                if(name==""||url==""){
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