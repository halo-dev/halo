<@compress single_line=true>
<link type="text/css" rel="stylesheet" href="/static/halo-content/css/comment.min.css">
<style>
${options.native_css!}
</style>
<#assign gavatarDefault="${options.comment_gavatar_default!'mm'}" />
<div class="comment-container">
    <div class="comment-avatar">
        <img src="//gravatar.loli.net/avatar/none?s=256&d=${gavatarDefault}" class="comment-author-avatar">
    </div>
    <div class="comment-wrap">
        <div class="comment-header">
            <input type="hidden" name="postId" value="${post.id?c}">
            <input type="hidden" name="parentId" id="parentId" value="0">
            <input type="text" class="comment-input comment-input-who" name="author" id="commentAuthor" placeholder="昵称(必填)">
            <input type="text" class="comment-input comment-input-email" name="email" id="commentAuthorEmail" onblur="loadAvatar()" placeholder="邮箱(选填)">
            <input type="text" class="comment-input comment-input-website" name="authorUrl" id="commentAuthorUrl" placeholder="网址(选填)">
        </div>
        <div class="comment-content">
            <textarea class="comment-input-content" name="content" id="commentContent" placeholder="${options.comment_content_placeholder!'赶快评论一个吧！'}"></textarea>
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
        <#if comments?? && comments.content?size gt 0>
            <#list comments.content?sort_by("createTime") as comment>
                <li class="comment-list-one" id="comment-id-${comment.id?c}" style="margin-left: 5px;">
                    <img class="comment-list-one-img" src="//gravatar.loli.net/avatar/${comment.gavatarMd5!}?s=256&d=${gavatarDefault}">
                    <section>
                        <div class="comment-list-one-head">
                            <a class="comment-list-one-head-name" rel="nofollow" href="${comment.authorUrl!}">${comment.author!}</a>
                            <span class="comment-ua-info" style="display: none">${comment.userAgent!}</span>
                            <#if comment.isAdmin==1>
                                <span class="comment-list-one-head-admin">博主</span>
                            </#if>
                        </div>
                        <div class="comment-list-one-content">
                            <p>${comment.content!}</p>
                        </div>
                        <div class="comment-list-one-footer">
                            <span class="comment-list-one-footer-time">${comment.createTime?string("yyyy-MM-dd HH:mm")}</span>
                            <span at="${comment.id?c}" class="comment-list-one-footer-reback">回复</span>
                        </div>
                    </section>
                    <#if comment.children?? && comment.children.content?size gt 0>
                        <@childComments comment.children></@childComments>
                    </#if>
                </li>
            </#list>
        </#if>
        </ul>
    </#macro>
    <ul class="comment-list" id="comments-list">
        <#if comments?? && comments.content?size gt 0>
            <#list comments.content?sort_by("createTime")?reverse as comment>
                <li class="comment-list-one" id="comment-id-${comment.id?c}">
                    <img class="comment-list-one-img" src="//gravatar.loli.net/avatar/${comment.gavatarMd5!}?s=256&d=${gavatarDefault}">
                    <section>
                        <div class="comment-list-one-head">
                            <a class="comment-list-one-head-name" rel="nofollow" href="${comment.authorUrl!}">${comment.author!}</a>
                            <span class="comment-ua-info" style="display: none">${comment.userAgent!}</span>
                            <#if comment.isAdmin==1>
                                <label class="comment-list-one-head-admin">博主</label>
                            </#if>
                        </div>
                        <div class="comment-list-one-content">
                            <p>${comment.content!}</p>
                        </div>
                        <div class="comment-list-one-footer">
                            <span class="comment-list-one-footer-time">${comment.createTime?string("yyyy-MM-dd HH:mm")}</span>
                            <span at="${comment.id?c}" class="comment-list-one-footer-reback">回复</span>
                        </div>
                    </section>
                    <#if comment.children?? && comment.children.content?size gt 0>
                        <@childComments comment.children></@childComments>
                    </#if>
                </li>
            </#list>
        </#if>
    </ul>
    <div class="native-nav" id="comment-nav">
        <#if comments.totalPages gt 1>
            <ol class="page-nav">
                <#if comments.hasPrevious()>
                <li>
                    <a href="?cp=${comments.number-1}#comments-list" title="上一页">←</a>
                </li>
                </#if>
                <li>
                    <#list rainbow as r>
                        <#if r == comments.number>
                            <a href="?cp=${comments.number}#comments-list" style="color: red;">${r}</a>
                        <#else>
                            <a href="?cp=${r}#comments-list">${r}</a>
                        </#if>
                    </#list>
                </li>
                <#if comments.hasNext()>
                <li>
                    <a href="?cp=${comments.number+1}#comments-list" title="下一页">→</a>
                </li>
                </#if>
            </ol>
        </#if>
    </div>
</div>
<script src="/static/halo-common/jquery/jquery.min.js"></script>
<script src="/static/halo-content/plugins/md5/md5.min.js"></script>
<script src="/static/halo-content/plugins/ua-parser/ua-parser.min.js"></script>
<script>
var gavatarDefault = "${options.native_comment_avatar!'mm'}";
</script>
<script src="/static/halo-content/js/comment.min.js"></script>
</@compress>
