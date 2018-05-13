<#include "module/_macro.ftl">
<@head title="${options.blog_title} | 后台管理：评论"></@head>
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
                    <a data-pjax="true" href="/admin"><i class="fa fa-dashboard"></i> 首页</a>
                </li>
                <li class="active">评论</li>
            </ol>
        </section>
        <section class="content container-fluid">
            <ul style="list-style: none;padding-left: 0">
                <li class="publish">
                    <a data-pjax="true" href="/admin/comments" <#if status==0>style="color: #000" </#if>>已发布<span class="count">(${publicCount?default("0")})</span></a>&nbsp;|&nbsp;
                </li>
                <li class="draft">
                    <a data-pjax="true" href="/admin/comments?status=1" <#if status==1>style="color: #000" </#if>>待审核<span class="count">(${checkCount?default("0")})</span></a>&nbsp;|&nbsp;
                </li>
                <li class="trash">
                    <a data-pjax="true" href="/admin/comments?status=2" <#if status==2>style="color: #000" </#if>>回收站<span class="count">(${trashCount?default("0")})</span></a>
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
                                    <th width="50%">评论内容</th>
                                    <th>评论页面</th>
                                    <th>日期</th>
                                    <th>操作</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#if comments.content?size gt 0>
                                        <#list comments.content as comment>
                                            <tr>
                                                <td><a href="${comment.commentAuthorUrl}" target="_blank">${comment.commentAuthor}</a></td>
                                                <td>${comment.commentContent}</td>
                                                <td>
                                                    <#if comment.post.postType == "post">
                                                        <a target="_blank" href="/archives/${comment.post.postUrl}">${comment.post.postTitle}</a>
                                                    <#else >
                                                        <a target="_blank" href="/p/${comment.post.postUrl}">${comment.post.postTitle}</a>
                                                    </#if>
                                                </td>
                                                <td>${comment.commentDate}</td>
                                                <td>
                                                    <#switch comment.commentStatus>
                                                        <#case 0>
                                                        <button class="btn btn-primary btn-xs " onclick="replyShow('${comment.commentId?c}','${comment.post.postId?c}')" <#if comment.isAdmin==1>disabled</#if>>回复</button>
                                                        <button class="btn btn-danger btn-xs " onclick="modelShow('/admin/comments/throw?commentId=${comment.commentId?c}&status=1','确定移动到回收站？')">丢弃</button>
                                                        <#break >
                                                        <#case 1>
                                                        <a data-pjax="true" class="btn btn-primary btn-xs " href="/admin/comments/revert?commentId=${comment.commentId?c}&status=1">通过</a>
                                                        <button class="btn btn-info btn-xs " onclick="replyShow('${comment.commentId?c}','${comment.post.postId?c}')">通过并回复</button>
                                                        <button class="btn btn-danger btn-xs " onclick="modelShow('/admin/comments/throw?commentId=${comment.commentId?c}&status=1','确定移动到回收站？')">丢弃</button>
                                                        <#break >
                                                        <#case 2>
                                                        <a data-pjax="true" class="btn btn-primary btn-xs " href="/admin/comments/revert?commentId=${comment.commentId?c}&status=2">还原</a>
                                                        <button class="btn btn-danger btn-xs " onclick="modelShow('/admin/comments/remove?commentId=${comment.commentId?c}&status=2','确定要永久删除？')">删除</button>
                                                        <#break >
                                                    </#switch>
                                                </td>
                                            </tr>
                                        </#list>
                                    <#else >
                                        <tr>
                                            <td colspan="5" style="text-align: center">暂无评论</td>
                                        </tr>
                                    </#if>
                                </tbody>
                            </table>
                        </div>
                        <div class="box-footer clearfix">
                            <div class="no-margin pull-left">
                                第${comments.number+1}/${comments.totalPages}页
                            </div>
                            <ul class="pagination no-margin pull-right">
                                <li><a data-pjax="true" class="btn btn-sm <#if !comments.hasPrevious()>disabled</#if>" href="/admin/comments?status=${status}">首页</a> </li>
                                <li><a data-pjax="true" class="btn btn-sm <#if !comments.hasPrevious()>disabled</#if>" href="/admin/comments?status=${status}&page=${comments.number-1}">上页</a></li>
                                <li><a data-pjax="true" class="btn btn-sm <#if !comments.hasNext()>disabled</#if>" href="/admin/comments?status=${status}&page=${comments.number+1}">下页</a></li>
                                <li><a data-pjax="true" class="btn btn-sm <#if !comments.hasNext()>disabled</#if>" href="/admin/comments?status=${status}&page=${comments.totalPages-1}">尾页</a> </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        <!-- 删除确认弹出层 -->
        <div class="modal fade" id="removeCommentModal">
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
        <!-- 回复弹出层 -->
        <div class="modal fade" id="commentReplyModal">
            <div class="modal-dialog">
                <div class="modal-content message_align">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span></button>
                        <h4 class="modal-title">回复</h4>
                    </div>
                    <form method="post" action="/admin/comments/reply">
                        <div class="modal-body">
                            <textarea class="form-control" rows="5" id="commentContent" name="commentContent" style="resize: none"></textarea>
                        </div>
                        <div class="modal-footer">
                            <input type="hidden" id="commentId" name="commentId" value=""/>
                            <input type="hidden" id="userAgent" name="userAgent" value=""/>
                            <input type="hidden" id="postId" name="postId" value="" />
                            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                            <button type="submit" class="btn btn-primary">确定</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <script>
            function modelShow(url,message) {
                $('#url').val(url);
                $('#message').html(message);
                $('#removeCommentModal').modal();
            }
            function removeIt(){
                var url=$.trim($("#url").val());
                window.location.href=url;
            }

            function replyShow(commentId,postId) {
                $('#userAgent').val(navigator.userAgent);
                $('#commentId').val(commentId);
                $('#postId').val(postId);
                $('#commentReplyModal').modal();
            }
        </script>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>