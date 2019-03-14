<@compress single_line=true>
<link type="text/css" rel="stylesheet" href="/static/halo-common/OwO/OwO.min.css">
<link type="text/css" rel="stylesheet" href="/static/halo-frontend/css/comment.min.css">
<style>
${options.native_css!}
</style>
<div class="comment-container">
    <div class="comment-avatar">
        <img src="//gravatar.loli.net/avatar/none?s=256&d=${options.native_comment_avatar!'mm'}" class="comment-author-avatar">
    </div>
    <div class="comment-wrap">
        <div class="comment-header">
            <input type="hidden" name="postId" value="${post.postId?c}">
            <input type="hidden" name="commentParent" id="commentParent" value="0">
            <input type="text" class="comment-input comment-input-who" name="commentAuthor" id="commentAuthor" placeholder="昵称(必填)">
            <input type="text" class="comment-input comment-input-email" name="commentAuthorEmail" id="commentAuthorEmail" onblur="loadAvatar()" placeholder="邮箱(选填)">
            <input type="text" class="comment-input comment-input-website" name="commentAuthorUrl" id="commentAuthorUrl" placeholder="网址(选填)">
        </div>
        <div class="comment-content">
            <textarea class="comment-input-content" name="commentContent" id="commentContent" placeholder="${options.native_comment_placeholder!'赶快评论一个吧！'}"></textarea>
            <div class="OwO"></div>
        </div>
        <div class="comment-footer">
            <button type="button" class="comment-cancel-reply" id="comment-cancel-reply" style="display: none;">取消回复</button>
            <button type="button" class="comment-submit" id="comment-submit">提交</button>
        </div>
    </div>
    <div class="comment-message" style="text-align: center;padding: 20px;display: none"></div>
    <div class="comment-info">
        <span id="comment-info-total" style="font-weight: 600">${commentsCount!0}</span>评论
    </div>
    <#macro childComments comments>
        <ul class="comment-list" style="margin-left: 30px; border-left: 1px solid #f1f1f1">
        <#if comments?? && comments?size gt 0>
            <#list comments?sort_by("commentDate") as comment>
                <li class="comment-list-one" id="comment-id-${comment.commentId?c}" style="margin-left: 5px;">
                    <img class="comment-list-one-img" src="//gravatar.loli.net/avatar/${comment.commentAuthorAvatarMd5!}?s=256&d=${options.native_comment_avatar!'mm'}">
                    <section>
                        <div class="comment-list-one-head">
                            <a class="comment-list-one-head-name" rel="nofollow" href="${comment.commentAuthorUrl!}">${comment.commentAuthor!}</a>
                            <span class="comment-ua-info" style="display: none">${comment.commentAgent!}</span>
                            <#if comment.isAdmin==1>
                                <span class="comment-list-one-head-admin">博主</span>
                            </#if>
                        </div>
                        <div class="comment-list-one-content">
                            <p>${comment.commentContent!}</p>
                        </div>
                        <div class="comment-list-one-footer">
                            <span class="comment-list-one-footer-time">${comment.commentDate?string("yyyy-MM-dd HH:mm")}</span>
                            <span at="${comment.commentId?c}" class="comment-list-one-footer-reback">回复</span>
                        </div>
                    </section>
                    <#if comment.childComments?? && comment.childComments?size gt 0>
                        <@childComments comment.childComments></@childComments>
                    </#if>
                </li>
            </#list>
        </#if>
        </ul>
    </#macro>
    <ul class="comment-list" id="comments-list">
        <#if comments?? && comments.getPageList()?size gt 0>
            <#list comments.getPageList()?sort_by("commentDate")?reverse as comment>
                <li class="comment-list-one" id="comment-id-${comment.commentId?c}">
                    <img class="comment-list-one-img" src="//gravatar.loli.net/avatar/${comment.commentAuthorAvatarMd5!}?s=256&d=${options.native_comment_avatar!'mm'}">
                    <section>
                        <div class="comment-list-one-head">
                            <a class="comment-list-one-head-name" rel="nofollow" href="${comment.commentAuthorUrl!}">${comment.commentAuthor!}</a>
                            <span class="comment-ua-info" style="display: none">${comment.commentAgent!}</span>
                            <#if comment.isAdmin==1>
                                <label class="comment-list-one-head-admin">博主</label>
                            </#if>
                        </div>
                        <div class="comment-list-one-content">
                            <p>${comment.commentContent!}</p>
                        </div>
                        <div class="comment-list-one-footer">
                            <span class="comment-list-one-footer-time">${comment.commentDate?string("yyyy-MM-dd HH:mm")}</span>
                            <span at="${comment.commentId?c}" class="comment-list-one-footer-reback">回复</span>
                        </div>
                    </section>
                    <#if comment.childComments?? && comment.childComments?size gt 0>
                        <@childComments comment.childComments></@childComments>
                    </#if>
                </li>
            </#list>
        </#if>
    </ul>
    <div class="native-nav" id="comment-nav">
        <#if comments.totalPage gt 1>
            <ol class="page-nav">
                <#if comments.hasPrevious>
                <li>
                    <a href="?cp=${comments.nowPage-1}#comments-list" title="上一页">←</a>
                </li>
                </#if>
                <li>
                    <#list rainbow as r>
                        <#if r == comments.nowPage>
                            <a href="?cp=${comments.nowPage}#comments-list" style="color: red;">${r}</a>
                        <#else>
                            <a href="?cp=${r}#comments-list">${r}</a>
                        </#if>
                    </#list>
                </li>
                <#if comments.hasNext>
                <li>
                    <a href="?cp=${comments.nowPage+1}#comments-list" title="下一页">→</a>
                </li>
                </#if>
            </ol>
        </#if>
    </div>
</div>
<script src="/static/halo-common/jquery/jquery.min.js"></script>
<script src="/static/halo-frontend/plugins/md5/md5.min.js"></script>
<script src="/static/halo-frontend/plugins/ua-parser/ua-parser.min.js"></script>
<script src="/static/halo-common/OwO/OwO.min.js"></script>
<#if (options.comment_activate_power_mode!'false') == "true">
<script src="/static/halo-frontend/plugins/activate-power-mode/activate-power-mode.js"></script>
<script>
    POWERMODE.colorful = true;
    POWERMODE.shake = false;
    document.body.addEventListener('input', POWERMODE);
</script>
</#if>
<script>
    var avatarType = "${options.native_comment_avatar!'mm'}";
</script>
<script src="/static/halo-frontend/js/comment.min.js"></script>
</@compress>
