<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title!} | <@spring.message code='admin.comments.title' /></@head>
<div class="content-wrapper">
    <link rel="stylesheet" href="/static/halo-common/OwO/OwO.min.css">
    <style type="text/css" rel="stylesheet">
        .draft,.publish,.trash{list-style:none;float:left;margin:0;padding-bottom:10px}s
    </style>
    <section class="content-header" id="animated-header">
        <h1><@spring.message code='admin.comments.title' /><small></small></h1>
        <ol class="breadcrumb">
            <li>
                <a data-pjax="true" href="/admin"><i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
            </li>
            <li class="active"><@spring.message code='admin.comments.title' /></li>
        </ol>
    </section>
    <section class="content container-fluid" id="animated-content">
        <ul style="list-style: none;padding-left: 0">
            <li class="publish">
                <a data-pjax="true" href="/admin/comments" <#if status==0>style="color: #000" </#if>><@spring.message code='common.status.published' /><span class="count">(${publicCount})</span></a>&nbsp;|&nbsp;
            </li>
            <li class="draft">
                <a data-pjax="true" href="/admin/comments?status=1" <#if status==1>style="color: #000" </#if>><@spring.message code='common.status.checking' /><span class="count">(${checkCount})</span></a>&nbsp;|&nbsp;
            </li>
            <li class="trash">
                <a data-pjax="true" href="/admin/comments?status=2" <#if status==2>style="color: #000" </#if>><@spring.message code='common.status.recycle-bin' /><span class="count">(${trashCount})</span></a>
            </li>
        </ul>
        <div class="row">
            <div class="col-xs-12">
                <div class="box box-primary">
                    <div class="box-body table-responsive no-padding">
                        <table class="table table-hover">
                            <tbody>
                                <tr>
                                    <th><@spring.message code='common.th.comment-author' /></th>
                                    <th width="50%"><@spring.message code='common.th.content' /></th>
                                    <th><@spring.message code='common.th.comment-page' /></th>
                                    <th><@spring.message code='common.th.date' /></th>
                                    <th><@spring.message code='common.th.control' /></th>
                                </tr>
                                <#if comments.content?size gt 0>
                                    <#list comments.content as comment>
                                        <tr>
                                            <td><a href="${comment.commentAuthorUrl}" target="_blank">${comment.commentAuthor}</a></td>
                                            <td><p>${comment.commentContent}</p></td>
                                            <td>
                                                <#if comment.post.postType == "post">
                                                    <a target="_blank" href="/archives/${comment.post.postUrl}#comment-id-${comment.commentId?c}">${comment.post.postTitle}</a>
                                                <#else >
                                                    <a target="_blank" href="/p/${comment.post.postUrl}#comment-id-${comment.commentId?c}">${comment.post.postTitle}</a>
                                                </#if>
                                            </td>
                                            <td>${comment.commentDate?string('yyyy-MM-dd HH:mm')}</td>
                                            <td>
                                                <#switch comment.commentStatus>
                                                    <#case 0>
                                                    <button class="btn btn-primary btn-xs " onclick="replyShow('${comment.commentId?c}','${comment.post.postId?c}')" <#if comment.isAdmin==1>disabled</#if>><@spring.message code="common.btn.reply" /></button>
                                                    <button class="btn btn-danger btn-xs " onclick="modelShow('/admin/comments/throw?commentId=${comment.commentId?c}&status=0&page=${comments.number}','<@spring.message code="common.text.tips.to-recycle-bin" />')"><@spring.message code="common.btn.recycling" /></button>
                                                    <#break >
                                                    <#case 1>
                                                    <a data-pjax="true" class="btn btn-primary btn-xs " href="/admin/comments/revert?commentId=${comment.commentId?c}&status=1"><@spring.message code="common.btn.pass" /></a>
                                                    <button class="btn btn-info btn-xs " onclick="replyShow('${comment.commentId?c}','${comment.post.postId?c}')"><@spring.message code="common.btn.pass-reply" /></button>
                                                    <button class="btn btn-danger btn-xs " onclick="modelShow('/admin/comments/throw?commentId=${comment.commentId?c}&status=1&page=${comments.number}','<@spring.message code="common.text.tips.to-recycle-bin" />')"><@spring.message code="common.btn.recycling" /></button>
                                                    <#break >
                                                    <#case 2>
                                                    <a data-pjax="true" class="btn btn-primary btn-xs " href="/admin/comments/revert?commentId=${comment.commentId?c}&status=2"><@spring.message code="common.btn.reduction" /></a>
                                                    <button class="btn btn-danger btn-xs " onclick="modelShow('/admin/comments/remove?commentId=${comment.commentId?c}&status=2&page=${comments.number}','<@spring.message code="common.text.tips.to-delete" />')"><@spring.message code="common.btn.delete" /></button>
                                                    <#break >
                                                </#switch>
                                            </td>
                                        </tr>
                                    </#list>
                                <#else >
                                    <tr>
                                        <td colspan="5" style="text-align: center"><@spring.message code="common.text.no-data" /></td>
                                    </tr>
                                </#if>
                            </tbody>
                        </table>
                    </div>
                    <div class="box-footer clearfix">
                        <div class="no-margin pull-left">
                            <@spring.message code='admin.pageinfo.text.no' />${comments.number+1}/${comments.totalPages}<@spring.message code='admin.pageinfo.text.page' />
                        </div>
                        <div class="btn-group pull-right btn-group-sm" role="group">
                            <a data-pjax="true" class="btn btn-default <#if !comments.hasPrevious()>disabled</#if>" href="/admin/comments?status=${status}">
                                <@spring.message code='admin.pageinfo.btn.first' />
                            </a>
                            <a data-pjax="true" class="btn btn-default <#if !comments.hasPrevious()>disabled</#if>" href="/admin/comments?status=${status}&page=${comments.number-1}">
                                <@spring.message code='admin.pageinfo.btn.pre' />
                            </a>
                            <a data-pjax="true" class="btn btn-default <#if !comments.hasNext()>disabled</#if>" href="/admin/comments?status=${status}&page=${comments.number+1}">
                                <@spring.message code='admin.pageinfo.btn.next' />
                            </a>
                            <a data-pjax="true" class="btn btn-default <#if !comments.hasNext()>disabled</#if>" href="/admin/comments?status=${status}&page=${comments.totalPages-1}">
                                <@spring.message code='admin.pageinfo.btn.last' />
                            </a>
                        </div>
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
                    <h4 class="modal-title"><@spring.message code="common.text.tips" /></h4>
                </div>
                <div class="modal-body">
                    <p id="message"></p>
                </div>
                <div class="modal-footer">
                    <input type="hidden" id="url"/>
                    <button type="button" class="btn btn-default" data-dismiss="modal"><@spring.message code="common.btn.cancel" /></button>
                    <a onclick="removeIt()" class="btn btn-danger" data-dismiss="modal"><@spring.message code="common.btn.define" /></a>
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
                    <h4 class="modal-title"><@spring.message code="common.btn.reply" /></h4>
                </div>
                <form>
                    <input type="hidden" id="commentId" name="commentId" value=""/>
                    <input type="hidden" id="userAgent" name="userAgent" value=""/>
                    <input type="hidden" id="postId" name="postId" value="" />
                    <div class="modal-body">
                        <textarea class="form-control comment-input-content" rows="5" id="commentContent" name="commentContent" style="resize: none"></textarea>
                        <div class="OwO"></div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal"><@spring.message code="common.btn.cancel" /></button>
                        <button type="button" class="btn btn-primary" onclick="reply()"><@spring.message code="common.btn.define" /></button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<@footer>
<script type="application/javascript" id="footer_script">
    var s = new OwO({
        container: document.getElementsByClassName('OwO')[0],
        target: document.getElementsByClassName('comment-input-content')[0],
        position: 'down',
        width: '100%',
        maxHeight: '210px',
        api:"/static/halo-common/OwO/OwO.min.json"
    });
    function modelShow(url,message) {
        $('#url').val(url);
        $('#message').html(message);
        $('#removeCommentModal').modal();
    }
    function removeIt(){
        var url=$.trim($("#url").val());
        <#if (options.admin_pjax!'true') == 'true'>
            pjax.loadUrl(url);
        <#else>
            window.location.href = url;
        </#if>
    }

    /**
     * 显示回复模态框
     *
     * @param commentId commentId
     * @param postId postId
     */
    function replyShow(commentId,postId) {
        $('#userAgent').val(navigator.userAgent);
        $('#commentId').val(commentId);
        $('#postId').val(postId);
        $('#commentReplyModal').modal();
    }

    function reply() {
        $.post('/admin/comments/reply',{
            'commentId': $("#commentId").val(),
            'userAgent': $("#userAgent").val(),
            'postId': $("#postId").val(),
            'commentContent': halo.formatContent($("#commentContent").val())
        },function(data) {
            if(data.code === 1){
                window.location.reload();
            }
        },'JSON');
    }
</script>
</@footer>
</#compress>
