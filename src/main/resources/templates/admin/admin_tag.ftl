<#compress >
<#include "module/_macro.ftl">
<@head title="Halo后台管理-标签">
</@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <link rel="stylesheet" href="/static/plugins/toast/css/jquery.toast.min.css">
        <section class="content-header">
            <h1>
                标签
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li>
                    <a href="#">
                        <i class="fa fa-dashboard"></i> 首页</a>
                </li>
                <li><a href="#">文章</a></li>
                <li class="active">标签</li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div class="row">
                <div class="col-md-5">
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">添加新标签</h3>
                        </div>
                        <form action="/admin/tag/save" method="post" role="form" onsubmit="return checkTag()">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="tagName">名称</label>
                                    <input type="text" class="form-control" id="tagName" name="tagName">
                                    <small>页面上所显示的名称</small>
                                </div>
                                <div class="form-group">
                                    <label for="tagUrl">路径名称</label>
                                    <input type="text" class="form-control" id="tagUrl" name="tagUrl">
                                    <small>*这是文章路径上显示的名称，最好为英文</small>
                                </div>
                            </div>
                            <div class="box-footer">
                                <button type="submit" class="btn btn-primary btn-flat">添加新标签</button>
                            </div>
                        </form>
                    </div>
                </div>
                <div class="col-md-7">
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">所有标签</h3>
                        </div>
                        <div class="box-body table-responsive">
                            <table class="table table-hover">
                                <thead>
                                <tr>
                                    <th>名称</th>
                                    <th>路径</th>
                                    <th>总数</th>
                                    <th>操作</th>
                                </tr>
                                </thead>
                                <tbody>
                                <#list tags as tag>
                                    <tr>
                                        <td>${tag.tagName}</td>
                                        <td>${tag.tagUrl}</td>
                                        <td>2</td>
                                        <td>
                                            <a class="btn btn-danger btn-xs btn-flat" href="/admin/tag/edit?tagId=${tag.tagId}">修改</a>
                                            <button class="btn btn-primary btn-xs btn-flat" onclick="modelShow('/admin/tag/remove?tagId=${tag.tagId}')">删除</button>
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
        <div class="modal fade" id="removeCateModal">
            <div class="modal-dialog">
                <div class="modal-content message_align">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span></button>
                        <h4 class="modal-title">提示信息</h4>
                    </div>
                    <div class="modal-body">
                        <p>您确认要删除吗？</p>
                    </div>
                    <div class="modal-footer">
                        <input type="hidden" id="url"/>
                        <button type="button" class="btn btn-default btn-flat" data-dismiss="modal">取消</button>
                        <a onclick="removeIt()" class="btn btn-danger btn-flat" data-dismiss="modal">确定</a>
                    </div>
                </div>
            </div>
        </div>
        <script src="/static/plugins/toast/js/jquery.toast.min.js"></script>
        <script>
            function modelShow(url) {
                $('#url').val(url);
                $('#removeCateModal').modal();
            }
            function removeIt(){
                var url=$.trim($("#url").val());
                window.location.href=url;
            }
            function checkTag() {
                var name = $('#tagName').val();
                var url = $('#tagUrl').val();
                var result = true;
                if(name==""||url==""){
                    showMsg("请输入完整信息！","info",2000);
                    result = false;
                }
                $.ajax({
                    type: 'GET',
                    url: '/admin/tag/checkUrl',
                    async: false,
                    data: {
                        'tagUrl' : url
                    },
                    success: function (data) {
                        if(data==true){
                            showMsg("该路径已经存在！","info",2000);
                            result = false;
                        }
                    }
                });
                return result;
            }
        </script>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>
</#compress>