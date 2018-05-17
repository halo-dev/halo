<#compress >
<#include "module/_macro.ftl">
<@head title="${options.blog_title} | 后台管理：页面">
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
            <a id="btnNewPage" href="/admin/page/new">
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
                        <div class="tab-content" style="padding: 0;">
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
                                                <a href="/links" class="btn btn-info btn-xs " target="_blank">预览</a>
                                                <a data-pjax="true" href="/admin/page/links" class="btn btn-primary btn-xs ">配置</a>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>图库页面</td>
                                            <td>/gallery</td>
                                            <td>
                                                <a href="/gallery" class="btn btn-info btn-xs " target="_blank">预览</a>
                                                <a data-pjax="true" href="/admin/page/galleries" class="btn btn-primary btn-xs ">配置</a>
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
                                                <th>评论</th>
                                                <td>访问量</td>
                                                <th>日期</th>
                                                <th>操作</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                        <#if pages?size gt 0>
                                            <#list pages as page>
                                                <tr>
                                                    <td>${page.postTitle}</td>
                                                    <td>/p/${page.postUrl}</td>
                                                    <td>${page.comments?size}</td>
                                                    <td>${page.postViews}</td>
                                                    <td>${page.postDate?string("yyyy-MM-dd HH:mm")}</td>
                                                    <td>
                                                        <a href="/p/${page.postUrl}" class="btn btn-info btn-xs " target="_blank">预览</a>
                                                        <a href="/admin/page/edit?pageId=${page.postId?c}" class="btn btn-primary btn-xs ">编辑</a>
                                                        <button class="btn btn-danger btn-xs " onclick="modelShow('/admin/posts/remove?postId=${page.postId?c}&postType=${page.postType}','确定永久删除？(不可逆)')">永久删除</button>
                                                    </td>
                                                </tr>
                                            </#list>
                                            <#else>
                                            <tr>
                                                <td colspan="6" style="text-align: center;">暂无页面</td>
                                            </tr>
                                        </#if>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        <!-- 删除确认弹出层 -->
        <div class="modal fade" id="removePostModal">
            <div class="modal-dialog">
                <div class="modal-content message_align">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span></button>
                        <h4 class="modal-title">提示信息</h4>
                    </div>
                    <div class="modal-body">
                        <p id="message"></p>
                    </div>
                    <div class="modal-footer">
                        <input type="hidden" id="url"/>
                        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                        <a onclick="removeIt()" class="btn btn-danger" data-dismiss="modal">确定</a>
                    </div>
                </div>
            </div>
        </div>
        <script>
            function modelShow(url,message) {
                $('#url').val(url);
                $('#message').html(message);
                $('#removePostModal').modal();
            }
            function removeIt(){
                var url=$.trim($("#url").val());
                window.location.href=url;
            }
        </script>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>
</#compress>