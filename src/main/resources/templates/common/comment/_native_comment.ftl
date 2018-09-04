<@compress single_line=true>
<link type="text/css" rel="stylesheet" href="/static/plugins/OwO/OwO.min.css">
<link type="text/css" rel="stylesheet" href="/static/css/comment.css">
<style>
${options.native_css?if_exists}
</style>
<div class="native-comment">
    <div class="comment-avatar">
        <img src="//gravatar.loli.net/avatar/none?s=256&d=${options.native_comment_avatar?default('mm')}" height="48" width="48" class="comment-author-avatar">
    </div>
    <div class="native-wrap">
        <div class="comment-header">
            <input type="hidden" name="postId" value="${post.postId?c}">
            <input type="hidden" name="commentParent" id="commentParent" value="0">
            <input type="text" class="comment-input comment-input-who" name="commentAuthor" id="commentAuthor" placeholder="昵称(必填)">
            <input type="text" class="comment-input comment-input-email" name="commentAuthorEmail" id="commentAuthorEmail" onblur="loadAvatar()" placeholder="邮箱(选填)">
            <input type="text" class="comment-input comment-input-website" name="commentAuthorUrl" id="commentAuthorUrl" placeholder="网址(选填)">
        </div>
        <div class="comment-content">
            <textarea class="comment-input-content" name="commentContent" id="commentContent"
                      placeholder="${options.native_comment_placeholder?default('赶快评论一个吧！')}"></textarea>
            <div class="OwO"></div>
        </div>
        <div class="comment-footer">
            <button type="button" class="comment-submit" id="btn-push">提交</button>
        </div>
    </div>
    <div class="native-message" style="text-align: center;padding: 20px;display: none"></div>
    <div class="native-info">
        <span id="native-info-total" style="font-weight: 600">${commentsCount?default(0)}</span>评论
    </div>
    <#macro childComments comments>
        <ul class="native-list" style="margin-left: 30px; border-left: 1px solid #f1f1f1">
        <#if comments?? && comments?size gt 0>
            <#list comments?sort_by("commentDate") as comment>
                <li class="native-list-one" id="comment-id-${comment.commentId?c}" style="margin-left: 5px;">
                    <img class="native-list-one-img" style="width: 2rem;height: 2rem;" src="//gravatar.loli.net/avatar/${comment.commentAuthorAvatarMd5?if_exists}?s=256&d=${options.native_comment_avatar?default('mm')}">
                    <section>
                        <div class="native-list-one-head">
                            <a class="native-list-one-head-name" rel="nofollow" href="${comment.commentAuthorUrl?if_exists}">${comment.commentAuthor?if_exists}</a>
                            <span class="native-comment-ua-info" style="display: none">${comment.commentAgent?if_exists}</span>
                            <#if comment.isAdmin==1>
                                <label class="native-list-one-head-admin">博主</label>
                            </#if>
                        </div>
                        <div class="native-list-one-content">
                            <p>${comment.commentContent?if_exists}</p>
                        </div>
                        <div class="native-list-one-footer">
                            <span class="native-list-one-footer-time">${comment.commentDate?string("yyyy-MM-dd HH:mm")}</span>
                            <span at="${comment.commentId?c}" class="native-list-one-footer-reback">回复</span>
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
    <ul class="native-list" id="comments-list">
        <#if comments?? && comments.getPageList()?size gt 0>
            <#list comments.getPageList()?sort_by("commentDate")?reverse as comment>
                <li class="native-list-one" id="comment-id-${comment.commentId?c}">
                    <img class="native-list-one-img" src="//gravatar.loli.net/avatar/${comment.commentAuthorAvatarMd5?if_exists}?s=256&d=${options.native_comment_avatar?default('mm')}">
                    <section>
                        <div class="native-list-one-head">
                            <a class="native-list-one-head-name" rel="nofollow" href="${comment.commentAuthorUrl?if_exists}">${comment.commentAuthor?if_exists}</a>
                            <span class="native-comment-ua-info" style="display: none">${comment.commentAgent?if_exists}</span>
                            <#if comment.isAdmin==1>
                                <label class="native-list-one-head-admin">博主</label>
                            </#if>
                        </div>
                        <div class="native-list-one-content">
                            <p>${comment.commentContent?if_exists}</p>
                        </div>
                        <div class="native-list-one-footer">
                            <span class="native-list-one-footer-time">${comment.commentDate?string("yyyy-MM-dd HH:mm")}</span>
                            <span at="${comment.commentId?c}" class="native-list-one-footer-reback">回复</span>
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
<script src="/static/plugins/jquery/jquery.min.js"></script>
<script src="/static/plugins/md5/md5.min.js"></script>
<script src="/static/plugins/ua-parser/ua-parser.min.js"></script>
<script src="/static/plugins/OwO/OwO.min.js"></script>
<script>
    var s = new OwO({
        logo: 'OωO表情',
        container: document.getElementsByClassName('OwO')[0],
        target: document.getElementsByClassName('comment-input-content')[0],
        position: 'down',
        width: '100%',
        maxHeight: '210px',
        api:"/static/plugins/OwO/OwO.min.json"
    });
    $(document).ready(function () {
        $(".native-list-one-head").each(function (i) {
            var uaInfo = $(this).children(".native-comment-ua-info").html();
            $(this).append(show_ua(uaInfo));
        });
        $("#commentAuthor").val(localStorage.getItem("author"));
        $("#commentAuthorEmail").val(localStorage.getItem("email"));
        $("#commentAuthorUrl").val(localStorage.getItem("url"));
        loadAvatar();
    });
    $('#btn-push').click(function () {
        var author = $("#commentAuthor");
        var content = $("#commentContent");
        var email = $("#commentAuthorEmail");
        var url = $("#commentAuthorUrl");
        if (author.val() == '' || content.val() == '') {
            $(".native-message").html("<span style='color:red'>请输入必填项！</span>");
            $(".native-message").fadeIn(1000);
            setTimeout(function () {
                $(".native-message").fadeOut(1000);
            },1500);
            return;
        }
        $(this).attr("disabled","disabled");
        $(this).html("提交中...");
        $.ajax({
            type: 'POST',
            url: '/newComment',
            async: false,
            data: {
                'postId': $('input[name=postId]').val(),
                'commentContent': $('textarea[name=commentContent]').val(),
                'commentAuthor': $('input[name=commentAuthor]').val(),
                'commentAuthorEmail': $('input[name=commentAuthorEmail]').val(),
                'commentAuthorUrl': $('input[name=commentAuthorUrl]').val(),
                'commentAgent': navigator.userAgent,
                'commentParent': $('input[name=commentParent]').val()
            },
            success: function (data) {
                localStorage.setItem('author', author.val());
                localStorage.setItem('email', email.val());
                localStorage.setItem('url', url.val());
                if(data.code==1){
                    $('.comment-input-content').val("");
                    $(".native-message").html("<span>"+data.msg+"</span>");
                }else{
                    $(".native-message").html("<span style='color:red'>"+data.msg+"</span>");
                }
                $(".native-message").fadeIn(1000);
                setTimeout(function () {
                    $(".native-message").fadeOut(1000);
                    $("#btn-push").removeAttr("disabled");
                    $("#btn-push").html("提交");
                    window.location.reload();
                },1500);
            }
        });
    });
    $('.native-list-one-footer-reback').click(function () {
        var at = $(this).attr("at");
        var commentParentAuthor = $('#comment-id-'+at).find(".native-list-one-head-name").html();
        $('#commentParent').val(at);
        $('#commentContent').val("@"+commentParentAuthor+": ");
        $('#commentContent').focus();
    });
    function loadAvatar() {
        $(".comment-author-avatar").attr("src","//gravatar.loli.net/avatar/"+md5(localStorage.getItem("email"))+"?s=256&d=${options.native_comment_avatar?default('mm')}");
        if($('input[name=commentAuthorEmail]').val()!='' && $('input[name=commentAuthorEmail]').val()!=null){
            $(".comment-author-avatar").attr("src","//gravatar.loli.net/avatar/"+md5($('input[name=commentAuthorEmail]').val())+"?s=256&d=${options.native_comment_avatar?default('mm')}");
        }
    }
    var parser = new UAParser();
    function show_ua(string){
        parser.setUA(string);
        var uua = parser.getResult();
        if(uua.os.version=='x86_64') {
            uua.os.version = 'x64';
        }
        var browser = uua.browser.name+' '+uua.browser.version;
        var os = uua.os.name + ' ' + uua.os.version;
        return '<span class="ua">'+browser+'</span><span class="ua">'+os+'</span>';
    }
</script>
</@compress>
