<style>
    *{
        box-sizing: border-box;
    }

    .native-comment{
        padding: 10px;
    }

    .native-wrap{
        border: 1px solid #f0f0f0;
        padding: 10px;
        overflow: hidden;
        position: relative;
    }

    input,textarea,button{
        outline: none;
    }

    .comment-header{
        width: 100%;
        line-height: 1.8;
    }

    .comment-input-who,.comment-input-email,.comment-input-website{
        width: 33.33%;
        padding: 10px 0;
        font-size: .8rem;
        float: left;
        border: none;
        border-bottom: 1px dashed #dedede;
    }

    .comment-input:focus{
        border-bottom: 1px dashed red;
    }

    .comment-input-content{
        width: 100%;
        min-height: 120px;
        resize: vertical;
        border: none;
        padding: 10px 0;
    }

    .comment-footer{
        text-align: right;
        vertical-align: middle;
    }

    .comment-submit{
        border-radius: 0;
        vertical-align: middle;
        padding: 7px 14px;
        font-size: .9rem;
        cursor: pointer;
        border: 1px solid #ededed;
        background: #ededed;
        color: #313131;
    }

    .native-list{
        width: 100%;
        list-style: none;
        margin: 0 auto;
        padding: 0;
    }

    .native-list .native-list-one{
        padding-top: 10px;
        position: relative;
        display: block;
        transition: all .3s ease-in-out;
    }

    .native-list .native-list-one .native-list-one-img{
        width: 2.5rem;
        height: 2.5rem;
        float: left;
        border-radius: 50%;
        margin-right: .7rem;
    }

    .native-list .native-list-one section{
        overflow: hidden;
        padding-bottom: 1.5rem;
        border-bottom: 1px dashed #f5f5f5;
    }

    .native-list .native-list-one section .native-list-one-head{
        line-height: 1.5;
        margin-bottom: .625rem;
        margin-top: 0;
    }

    .native-list-one-head-name{
        font-size: .875rem;
        font-weight: 700;
        margin-right: .875rem;
        cursor: pointer;
        text-decoration: none;
        color: #555;
    }

    .ua{
        display: inline-block;
        padding: .2rem .5rem;
        background: #ededed;
        color: #b3b1b1;
        font-size: .75rem;
        border-radius: .2rem;
        margin-right: .3rem;
    }

    .native-list-one-content p{
        font-size: 14px;
        letter-spacing: 0;
        margin: 0 0 1pc;
        font-weight: 400;
    }

    .native-list-one-footer-time{
        color: #b3b3b3;
        font-size: .75rem;
        margin-right: .875rem;
    }

    .native-list-one-footer-reback{
        font-size: .8125rem;
        color: #ef2f11;
        cursor: pointer;
    }
    ${options.native_css?if_exists}
    @media screen and (max-width:560px){
        .comment-input-who,.comment-input-email,.comment-input-website{
            width: 100%;
        }
    }
</style>
<div class="native-comment">
    <div class="native-wrap">
        <div class="comment-header">
            <input type="hidden" name="postId" value="${post.postId}">
            <input type="text" class="comment-input comment-input-who" name="commentAuthor" id="commentAuthor" placeholder="昵称">
            <input type="text" class="comment-input comment-input-email" name="commentAuthorEmail" placeholder="邮箱">
            <input type="text" class="comment-input comment-input-website" name="commentAuthorUrl" placeholder="网址(https/http)">
        </div>
        <div class="comment-content">
            <textarea class="comment-input comment-input-content" name="commentContent" id="commentContent" placeholder="come on"></textarea>
        </div>
        <div class="comment-footer">
            <button type="button" class="comment-submit" id="btn-push">提交</button>
        </div>
    </div>
    <div class="native-info" style="padding-top: 5px;font-size: 12px;color: #0F192A;">
        <span id="native-info-total">${post.comments?size}</span>条评论
    </div>
    <ul class="native-list">

    </ul>
    <div class="native-loading" style="text-align: center;padding-top: 5px">
        <img src="/static/images/tail-spin.svg" width="36" alt="">
    </div>
</div>
<script src="//cdn.bootcss.com/jquery/3.3.1/jquery.min.js"></script>
<script src="//cdn.bootcss.com/blueimp-md5/2.10.0/js/md5.min.js"></script>
<script src="//cdn.bootcss.com/UAParser.js/0.7.17/ua-parser.min.js"></script>
<script>
    $(document).ready(function(){
        $.ajax({
            type: "get",
            async: true,
            url: "/getComment/${post.postId}",
            dataType: "json",
            success: function(data){
                setTimeout(function(){
                    $('.native-loading').hide();
                },1000);
                var parser = new UAParser();
                $.each(data,function(i,element){
                    parser.setUA(element.commentAgent);
                    var result = parser.getResult();
                    var browser = result.browser.name+' '+result.browser.version;
                    var os = result.os.name + ' ' + result.os.version;
                    var author = element.commentAuthor;
                    var authorEmail = element.commentAuthorEmail;
                    var authorUrl = element.commentAuthorUrl;
                    var timestamp = element.commentDate;
                    var date = new Date(timestamp).toLocaleDateString();
                    var content = element.commentContent;
                    var authorPic = md5(authorEmail);
                    $('.native-list').append("<li class=\"native-list-one\"><img class=\"native-list-one-img\" src=\"//www.gravatar.com/avatar/"+authorPic+"?s=256&d=${options.native_comment_avatar?default('default')}\"><section><div class=\"native-list-one-head\"><a class=\"native-list-one-head-name\" rel=\"nofollow\" href=\""+authorUrl+"\" target=\"_blank\">"+author+"</a> <span class=\"ua\">"+browser+"</span> <span class=\"ua\">"+os+"</span></div><div class=\"native-list-one-content\"><p>"+content+"</p></div><div class=\"native-list-one-footer\"><span class=\"native-list-one-footer-time\">"+date+"</span> <span rid=\"\" at=\"@"+author+"\" mail=\""+authorEmail+"\" class=\"native-list-one-footer-reback\">回复</span></div></section></li>");
                });
            }
        });
    });
    $('#btn-push').click(function () {
        var author = $("#commentAuthor");
        if(author.val()==''){
            $(author).css("border-bottom","1px dashed red");
            return;
        }
        var content = $("#commentContent");
        if(content.val()==''){
            $(content).css("border-bottom","1px dashed red");
            return;
        }
        $.ajax({
            type: 'POST',
            url: '/newComment',
            async: false,
            data:{
                'postId' : $('input[name=postId]').val(),
                'commentContent' : $('textarea[name=commentContent]').val(),
                'commentAuthor' : $('input[name=commentAuthor]').val(),
                'commentAuthorEmail' : $('input[name=commentAuthorEmail]').val(),
                'commentAuthorUrl' : $('input[name=commentAuthorUrl]').val(),
                'commentAgent' : navigator.userAgent,
                'commentAuthorAvatarMd5' : md5($('input[name=commentAuthorEmail]').val())
            },
            success: function (data) {
                if(data==true){
                    window.location.reload();
                }
            }
        });
    });
</script>