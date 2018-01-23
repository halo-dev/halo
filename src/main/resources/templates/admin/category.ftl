<#compress >
<#include "module/_macro.ftl">
<@head title="Halo后台管理-分类目录"></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <link rel="stylesheet" href="/static/plugins/toast/css/jquery.toast.min.css">
        <section class="content-header">
            <h1>
                分类目录
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li>
                    <a href="/admin">
                        <i class="fa fa-dashboard"></i> 首页</a>
                </li>
                <li class="active">分类目录</li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div class="row">
                <div class="col-md-5">
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">添加分类目录</h3>
                        </div>
                        <form action="/admin/category/save" method="post" role="form" id="cateAddForm" onsubmit="return checkCate()">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="exampleInputEmail1">名称</label>
                                    <input type="text" class="form-control" id="cateName" name="cateName" placeholder="">
                                    <small>页面上所显示的名称</small>
                                </div>
                                <div class="form-group">
                                    <label for="exampleInputPassword1">路径名称</label>
                                    <input type="text" class="form-control" id="cateUrl" name="cateUrl" placeholder="">
                                    <small>*这是文章路径上显示的名称，最好为英文</small>
                                </div>
                                <div class="form-group">
                                    <label for="cateDesc" class="control-label">描述</label>
                                    <textarea class="form-control" rows="3" id="cateDesc" name="cateDesc" style="resize: none"></textarea>
                                    <small>*添加描述，部分主题可显示</small>
                                </div>
                            </div>
                            <div class="box-footer">
                                <button type="submit" class="btn btn-primary btn-flat">确定添加</button>
                            </div>
                        </form>
                    </div>
                </div>
                <div class="col-md-7">
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">所有分类目录</h3>
                        </div>
                        <div class="box-body table-responsive">
                            <table class="table table-hover">
                                <thead>
                                <tr>
                                    <th>名称</th>
                                    <th>路径</th>
                                    <th>描述</th>
                                    <th>文章数</th>
                                    <th>操作</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#list categories as cate>
                                    <tr>
                                        <td>${cate.cateName}</td>
                                        <td>${cate.cateUrl}</td>
                                        <td>${(cate.cateDesc)!}</td>
                                        <td>2</td>
                                        <td>
                                            <a href="/admin/category/edit?cateId=${cate.cateId}" class="btn btn-primary btn-xs btn-flat">修改</a>
                                            <button class="btn btn-danger btn-xs btn-flat" onclick="modelShow('/admin/category/remove?cateId=${cate.cateId}')">删除</button>
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
            function checkCate() {
                var name = $('#cateName').val();
                var url = $('#cateUrl').val();
                var desc = $('#cateDesc').val();
                var result = true;
                if(name==""||url==""||desc==""){
                    showMsg("请输入完整信息！","info",2000);
                    result = false;
                }
                $.ajax({
                    type: 'GET',
                    url: '/admin/category/checkUrl',
                    async: false,
                    data: {
                        'cateUrl' : url
                    },
                    success: function (data) {
                        if(data=="exists"){
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