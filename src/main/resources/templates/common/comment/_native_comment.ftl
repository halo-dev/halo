<link type="text/css" rel="stylesheet" href="/static/plugins/loaders/loaders.css">
<style>
    * {
        box-sizing: border-box;
    }
    .native-list-one-head a, .native-list-one-content a{
        opacity: 1;!important;
        -webkit-transition: all .15s linear;
        -moz-transition: all .15s linear;
        -o-transition: all .15s linear;
        -ms-transition: all .15s linear;
        transition: all .15s linear;
        /* color: #424242; */
        color: #7575d0;!important;
    }
    .native-comment {
        padding: 10px;
    }

    .native-wrap {
        border: 1px solid #f0f0f0;
        padding: 10px;
        overflow: hidden;
        position: relative;
        margin-left: 58px;
    }

    input, textarea, button {
        outline: none;
    }

    .comment-submit,.native-list-one-img,.native-list-one-footer-time,.native-list-one-footer-reback,.native-info,.native-nav,.ua,.native-message{
        -webkit-user-select:none;
        -moz-user-select:none;
        -ms-user-select:none;
        user-select:none;
    }

    .comment-header {
        width: 100%;
        line-height: 1.8;
    }

    .comment-input-who, .comment-input-email, .comment-input-website {
        width: 33.33% !important;
        padding: 10px 0 !important;
        font-size: .9rem !important;
        float: left !important;
        border: none !important;
        border-bottom: 1px dashed #dedede !important;
    }

    .comment-input:focus {
        border-bottom: 1px dashed red !important;
    }

    .comment-input-content {
        width: 100% !important;
        min-height: 120px !important;
        resize: vertical!important;
        border: none!important;
        font-size: .9rem !important;
        padding: 10px 0!important;
    }

    .comment-footer {
        text-align: right;
        vertical-align: middle;
        padding-top: 10px;
    }

    .comment-submit {
        border-radius: 0 !important;
        vertical-align: middle!important;
        padding: 7px 14px!important;
        font-size: .9rem!important;
        cursor: pointer!important;
        border: 1px solid #ededed!important;
        background: #ededed!important;
        color: #313131!important;
        transition: all .3s ease-in-out;
    }

    .comment-submit:hover{
        background-color: #fff !important;
        border-radius: 1.9rem !important;
        border-color: #859cff !important;
        color: #859cff !important;
    }

    .native-list {
        width: 100%;
        list-style: none;
        margin: 0 auto;
        padding: 0;
    }

    .native-list .native-list-one {
        padding-top: 10px;
        position: relative;
        display: block;
        transition: all .3s ease-in-out;
    }

    .native-list .native-list-one .native-list-one-img {
        width: 2.5rem;
        height: 2.5rem;
        float: left;
        border-radius: 50%;
        margin-right: .7rem;
        cursor: pointer;
        -webkit-transition: 0.4s;
        -webkit-transition: -webkit-transform 0.4s ease-out;
        transition: transform 0.4s ease-out;
        -moz-transition: -moz-transform 0.4s ease-out;
    }

    .native-list-one-img:hover{
        transform: rotate(360deg);
    }

    .native-list .native-list-one section {
        overflow: hidden;
        padding-bottom: 1.5rem;
        border-bottom: 1px dashed #f5f5f5;
    }

    .native-list .native-list-one section .native-list-one-head {
        line-height: 1.5;
        margin-bottom: .625rem;
        margin-top: 0;
    }

    .native-list-one-head-name {
        font-size: .875rem;
        font-weight: 700;
        cursor: pointer;
        text-decoration: none;
        color: #555;
    }

    .native-list-one-head-admin{
        padding: .1em 0.2em;
        border-radius: 2px;
        font-size: 70%;
        font-weight: 700;
        background-color: #87ceeb;
        color: #fff;
        display: inline;
    }

    .ua {
        display: inline-block;
        padding: .2rem .5rem;
        background: #ededed;
        color: #b3b1b1;
        font-size: .75rem;
        border-radius: .2rem;
        margin-right: .3rem;
    }

    .native-list-one-content p {
        font-size: 14px;
        letter-spacing: 0;
        margin: 0 0 1pc;
        font-weight: 400;
    }

    .native-list-one-footer-time {
        color: #b3b3b3;
        font-size: .75rem;
        margin-right: .875rem;
    }

    .native-list-one-footer-reback {
        font-size: .8125rem;
        color: #ef2f11;
        cursor: pointer;
    }
    .native-info{
        padding-top: 10px;
        font-size: 12px;
        color: #555;
    }
    .comment-avatar{
        position: relative;
        float: left;
    }
    .comment-avatar img{
        border-radius: 100%;
        -webkit-transition: 0.4s;
        -webkit-transition: -webkit-transform 0.4s ease-out;
        transition: transform 0.4s ease-out;
        -moz-transition: -moz-transform 0.4s ease-out;
        cursor: pointer;
    }

    .comment-avatar img:hover{
        transform: rotate(360deg);
    }

    .native-nav{
        padding: 10px 0;
    }
    .page-nav{
        margin: 20px 0;
        padding: 0 10px;
        list-style: none;
        text-align: center;
    }
    .page-nav li{
        display: inline-block;
    }
    ${options.native_css?if_exists}
    @media screen and (max-width: 560px) {
        .comment-input-who, .comment-input-email, .comment-input-website {
            width: 100% !important;
        }
    }
</style>
<div class="native-comment">
    <div class="comment-avatar">
        <img src="//www.gravatar.com/avatar/none?s=256&d=${options.native_comment_avatar?default('mm')}" height="48" width="48" class="comment-author-avatar">
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
        </div>
        <div class="comment-footer">
            <button type="button" class="comment-submit" id="btn-push">提交</button>
        </div>
    </div>
    <div class="native-message" style="text-align: center;padding: 20px;display: none"></div>
    <div class="native-info">
        <span id="native-info-total" style="font-weight: 600">${comments.getTotalElements()}</span>评论
    </div>
    <ul class="native-list">
        <#if comments.content?? && comments.content?size gt 0>
            <#list comments.content as comment>
                <li class="native-list-one" id="comment-id-${comment.commentId?c}">
                    <img class="native-list-one-img" src="//www.gravatar.com/avatar/${comment.commentAuthorAvatarMd5?if_exists}?s=256&d=${options.native_comment_avatar?default('mm')}">
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
                </li>
            </#list>
        </#if>
    </ul>
    <#--<div class="native-nav">-->
        <#--<ol class="page-nav">-->
            <#--<li>←</li>-->
            <#--<li>1</li>-->
            <#--<li>→</li>-->
        <#--</ol>-->
    <#--</div>-->
</div>
<script src="/static/plugins/jquery/jquery.min.js"></script>
<script src="/static/plugins/md5/md5.min.js"></script>
<script src="/static/plugins/ua-parser/ua-parser.min.js"></script>
<script>
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
        var url = $("#commentAuthorUrl")
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
                'commentAuthorAvatarMd5': md5($('input[name=commentAuthorEmail]').val()),
                'commentParent': $('input[name=commentParent]').val()
            },
            success: function (data) {
                localStorage.setItem('author', author.val());
                localStorage.setItem('email', email.val());
                localStorage.setItem('url', url.val());
                if(data.code==1){
                    $('.comment-input-content').val("");
                }
                $(".native-message").html("<span>"+data.msg+"</span>");
                $(".native-message").fadeIn(1000);
                setTimeout(function () {
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
        $(".comment-author-avatar").attr("src","//www.gravatar.com/avatar/"+md5(localStorage.getItem("email"))+"?s=256&d=${options.native_comment_avatar?default('mm')}");
        if($('input[name=commentAuthorEmail]').val()!='' && $('input[name=commentAuthorEmail]').val()!=null){
            $(".comment-author-avatar").attr("src","//www.gravatar.com/avatar/"+md5($('input[name=commentAuthorEmail]').val())+"?s=256&d=${options.native_comment_avatar?default('mm')}");
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