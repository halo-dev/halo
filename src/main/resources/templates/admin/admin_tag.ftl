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
        <style>
            .tag-cloud .label{
                padding: .3em .9em .45em;
                font-size: 100%;
                font-weight: 400;
            }
            .tag-cloud{
                margin-bottom: 11px;
                margin-right: 3px;
                display: inline-block;
                float: left;
            }
            .bg-color-1{
                background-color: #3c8dbc;
            }
            .bg-color-2{
                background-color: #00a65a;
            }
            .bg-color-3{
                background-color: #001f3f;
            }
            .bg-color-4{
                background-color: #39cccc;
            }
            .bg-color-5{
                background-color: #3d9970;
            }
            .bg-color-6{
                background-color: #01ff70;
            }
            .bg-color-7{
                background-color: #ff851b;
            }
            .bg-color-8{
                background-color: #f012be;
            }
            .bg-color-9{
                background-color: #605ca8;
            }
            .bg-color-10{
                background-color: #d81b60;
            }
            .bg-color-11{
                background-color: #dd4b39;
            }
            .bg-color-12{
                background-color: #f39c12;
            }
            .bg-color-13{
                background-color: #00c0ef;
            }
            .bg-color-14{
                background-color: #0073b7;
            }
            .bg-color-15{
                background-color: #111111;
            }
        </style>
        <section class="content-header">
            <h1>
                标签
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li>
                    <a data-pjax="true" href="#">
                        <i class="fa fa-dashboard"></i> 首页</a>
                </li>
                <li><a data-pjax="true" href="#">文章</a></li>
                <li class="active">标签</li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div class="row">
                <div class="col-md-5">
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">${statusName}标签<#if tag??>[${updateTag.tagName}]</#if></h3>
                        </div>
                        <#if updateTag??>
                            <form action="/admin/tag/save" method="post" role="form">
                                <input type="hidden" name="tagId" value="${updateTag.tagId}">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label for="tagName">名称</label>
                                        <input type="text" class="form-control" id="tagName" name="tagName" value="${updateTag.tagName}">
                                        <small>页面上所显示的名称</small>
                                    </div>
                                    <div class="form-group">
                                        <label for="tagUrl">路径名称</label>
                                        <input type="text" class="form-control" id="tagUrl" name="tagUrl" value="${updateTag.tagUrl}">
                                        <small>*这是文章路径上显示的名称，最好为英文</small>
                                    </div>
                                </div>
                                <div class="box-footer">
                                    <button type="submit" class="btn btn-primary btn-flat">确定${statusName}</button>
                                </div>
                            </form>
                        <#else >
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
                                    <button type="submit" class="btn btn-primary btn-flat">确定${statusName}</button>
                                </div>
                            </form>
                        </#if>
                    </div>
                </div>
                <div class="col-md-7">
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">所有标签</h3>
                        </div>
                        <div class="box-body table-responsive">
                            <#--<table class="table table-hover">-->
                                <#--<thead>-->
                                <#--<tr>-->
                                    <#--<th>名称</th>-->
                                    <#--<th>路径</th>-->
                                    <#--<th>总数</th>-->
                                    <#--<th>操作</th>-->
                                <#--</tr>-->
                                <#--</thead>-->
                                <#--<tbody>-->
                                <#--<#list tags as tag>-->
                                    <#--<tr>-->
                                        <#--<td>${tag.tagName}</td>-->
                                        <#--<td>${tag.tagUrl}</td>-->
                                        <#--<td>2</td>-->
                                        <#--<td>-->
                                            <#--<#if updateTag ?? && tag.tagId==updateTag.tagId>-->
                                                <#--<a class="btn btn-primary btn-xs btn-flat" href="#" disabled>正在修改</a>-->
                                            <#--<#else >-->
                                            <#--<a data-pjax="true" class="btn btn-primary btn-xs btn-flat" href="/admin/tag/edit?tagId=${tag.tagId}">修改</a>-->
                                            <#--</#if>-->
                                            <#--<button class="btn btn-danger btn-xs btn-flat" onclick="modelShow('/admin/tag/remove?tagId=${tag.tagId}')">删除</button>-->
                                        <#--</td>-->
                                    <#--</tr>-->
                                <#--</#list>-->
                                <#--</tbody>-->
                            <#--</table>-->
                            <#list tags as tag>
                                <div class="tag-cloud">
                                    <a class="tag-link" data-pjax="true" href="/admin/tag/edit?tagId=${tag.tagId}">
                                        <span class="label">${tag.tagName}( ${tag.posts?size} )</span>
                                    </a>
                                </div>
                            </#list>
                            <script>
                                var randomNum;
                                var tagLabel = $('.tag-link');
                                <#--for(var i = 0; i < ${tags?size}; i++) {-->
                                    <#--randomNum = Math.floor(Math. random() * 15 + 1);-->
                                    <#--tagLabel.children('.label').addClass("bg-color-"+randomNum);-->
                                    <#--tagLabel = tagLabel.next();-->
                                <#--}-->

                                $(".label").each(function () {
                                    randomNum = Math.floor(Math. random() * 15 + 1);
                                    $(this).addClass("bg-color-"+randomNum);
                                });
                            </script>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        <!-- 删除确认弹出层 -->
        <div class="modal fade label-primary" id="removeCateModal">
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