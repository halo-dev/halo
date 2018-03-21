<#include "module/_macro.ftl">
<@head title="Halo后台管理-评论管理"></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <style type="text/css" rel="stylesheet">
            .draft,.publish,.trash{list-style:none;float:left;margin:0;padding-bottom:10px}s
        </style>
        <section class="content-header">
            <h1>评论<small></small></h1>
            <ol class="breadcrumb">
                <li>
                    <a href="/admin"><i class="fa fa-dashboard"></i> 首页</a>
                </li>
                <li class="active">评论</li>
            </ol>
        </section>
        <section class="content container-fluid">
            <ul style="list-style: none;padding-left: 0">
                <li class="publish">
                    <a href="/admin/comments">已发布<span class="count">(${publicCount?default("0")})</span></a>&nbsp;|&nbsp;
                </li>
                <li class="draft">
                    <a href="/admin/comments?status=1">待审核<span class="count">(${checkCount?default("0")})</span></a>&nbsp;|&nbsp;
                </li>
                <li class="trash">
                    <a href="/admin/comments?status=2">回收站<span class="count">(${trashCount?default("0")})</span></a>
                </li>
            </ul>
            <div class="row">
                <div class="col-xs-12">
                    <div class="box box-primary">
                        <div class="box-body table-responsive">
                            <table class="table table-bordered table-hover">
                                <thead>
                                <tr>
                                    <th>评论者</th>
                                    <th>评论内容</th>
                                    <th>评论页面</th>
                                    <th>日期</th>
                                    <th>操作</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#list comments.content as comment>
                                        <tr>
                                            <td><a href="${comment.commentAuthorUrl}" target="_blank">${comment.commentAuthor}</a></td>
                                            <td>${comment.commentContent}</td>
                                            <td>
                                                <a target="_blank" href="/article/${comment.post.postUrl}">${comment.post.postTitle}</a>
                                            </td>
                                            <td>${comment.commentDate}</td>
                                            <td>
                                                <#switch comment.commentStatus>
                                                    <#case 0>
                                                    <button class="btn btn-info btn-sm btn-flat" onclick="modelShow()">回复</button>
                                                    <button class="btn btn-danger btn-sm btn-flat" onclick="modelShow('/admin/comments/throw?commentId=${comment.commentId}','确定移动到回收站？')">丢弃</button>
                                                    <#break >
                                                    <#case 1>
                                                    <a class="btn btn-info btn-sm btn-flat" href="/admin/comments/revert?commentId=${comment.commentId}&status=1">通过</a>
                                                    <button class="btn btn-danger btn-sm btn-flat" onclick="modelShow('/admin/comments/throw?commentId=${comment.commentId}','确定移动到回收站？')">丢弃</button>
                                                    <#break >
                                                    <#case 2>
                                                    <a class="btn btn-info btn-sm btn-flat" href="/admin/comments/revert?commentId=${comment.commentId}&status=2">还原</a>
                                                    <button class="btn btn-danger btn-sm btn-flat" onclick="modelShow('/admin/comments/remove?commentId=${comment.commentId}&status=2','确定要永久删除？')">删除</button>
                                                    <#break >
                                                </#switch>
                                            </td>
                                        </tr>
                                    </#list>
                                </tbody>
                            </table>
                        </div>
                        <div class="box-footer clearfix">
                            <div class="no-margin pull-left">
                                第${comments.number+1}/${comments.totalPages}页
                            </div>
                            <ul class="pagination no-margin pull-right">
                                <li><a class="btn btn-sm <#if !comments.hasPrevious()>disabled</#if>" href="/admin/comments?status=${status}">首页</a> </li>
                                <li><a class="btn btn-sm <#if !comments.hasPrevious()>disabled</#if>" href="/admin/comments?status=${status}&page=${comments.number-1}">上页</a></li>
                                <li><a class="btn btn-sm <#if !comments.hasNext()>disabled</#if>" href="/admin/comments?status=${status}&page=${comments.number+1}">下页</a></li>
                                <li><a class="btn btn-sm <#if !comments.hasNext()>disabled</#if>" href="/admin/comments?status=${status}&page=${comments.totalPages-1}">尾页</a> </li>
                            </ul>
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