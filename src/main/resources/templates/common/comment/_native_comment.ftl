<style>
    input[type=text],textarea{
        width: 100%;
        outline: none;
        font-size: 16px;
    }
    input[type=text]{
        height: 40px;
        font-size: 16px;
        opacity: 0.8;
        border-top: none;
        border-left: none;
        border-right: none;
        border-bottom: 1px dashed #70c09f;
    }
    input[type=text]:focus{
        border-bottom: 1px dashed red;
    }
    textarea{
        opacity: 0.8;
        border: 1px dashed #70c09f;
    }
    #native-comment{
        padding : 10px;
    }
    #inputAuthor,#inputAuthorEmail,#inputAuthorUrl,#inputContent{
        padding: 0;
        margin-bottom: 10px;
    }
    #btn-push{
        border: 1px solid #000;
        background: transparent;
        width: 65px;
        height: 35px;
        transition: all .5s ease-in-out;
    }
    #btn-push:hover{
        border: none;
        background: #000;
        color: #fff;
    }

    #comments-list ul{
        width: 100%;
        list-style: none;
        margin: 0 auto;
        padding: 0;
        font-weight: 400;
        letter-spacing: 0;
        font-size: 14px;
    }
    .vcard{
        padding-top: 1.5rem;
        position: relative;
        display: block;
        list-style: none;
        margin: 0 auto;
        padding: 0;
    }
    .vimg{
        width: 2.5rem;
        height: 2.5rem;
        float: left;
        border-radius: 50%;
        margin-right: .7525rem;
    }
    .v .vlist .vcard section {
        overflow: hidden;
        padding-bottom: 1.5rem;
        border-bottom: 1px dashed #f5f5f5;
    }
     .vhead {
        line-height: 1.5;
        margin-bottom: .625rem;
        margin-top: 0;
    }
     .vcontent {
        word-wrap: break-word;
        word-break: break-all;
        text-align: justify;
        color: #4a4a4a;
        font-size: .875rem;
        line-height: 2;
        position: relative;
        margin-bottom: .75rem;
    }
    .vsys {
        display: inline-block;
        padding: .2rem .5rem;
        background: #ededed;
        color: #b3b1b1;
        font-size: .75rem;
        border-radius: .2rem;
        margin-right: .3rem;
    }
    .vtime {
        color: #b3b3b3;
        font-size: .75rem;
        margin-right: .875rem;
    }
</style>
<div id="native-comment">
    <div class="row" style="margin: 0">
        <form action="/newComment" method="post" id="comment-form">
            <input type="hidden" name="postId" value="${post.postId}">
            <div class="col-lg-12 col-xs-12" id="inputContent">
                <textarea rows="5" name="commentContent" placeholder="赶快评论一个吧！" style="resize: none;"></textarea>
            </div>
            <div class="col-lg-4 col-xs-12" id="inputAuthor">
                <input name="commentAuthor" type="text" placeholder="昵称">
            </div>
            <div class="col-lg-4 col-xs-12" id="inputAuthorEmail">
                <input name="commentAuthorEmail" type="text" placeholder="邮箱">
            </div>
            <div class="col-lg-4 col-xs-12" id="inputAuthorUrl">
                <input name="commentAuthorUrl" type="text" placeholder="链接(http/https)">
            </div>
            <div class="col-lg-12" style="padding: 0;">
                <input class="pull-right" type="button" id="btn-push" value="提交">
            </div>
        </form>
        <div class="commentCount" style="display:block;">
            <div class="vcount col">
                <span class="vnum">${comments.totalElements}</span> 评论
            </div>
        </div>
        <div class="col-lg-12" style="padding: 0;">
            <div id="comments-list">
                <ul>
                    <#list comments.content as comment>
                    <li class="vcard">
                        <img class="vimg" src="https://gravatar.cat.net/avatar/36a181c9a5a9ab58f0212a216ac677f4?d=identicon">
                        <section>
                            <div class="vhead">
                                <a class="vname" rel="nofollow" href="https://ryanc.cc" target="_blank">${comment.commentAuthor}</a>
                                <span class="vsys">Chrome 63.0.3239.84</span>
                                <span class="vsys">Mac OS 10.12.6</span>
                            </div>
                            <div class="vcontent">
                                <p>${comment.commentContent?if_exists}</p>
                            </div>
                            <div class="vfooter">
                                <span class="vtime">${comment.commentDate?string("yyyy-MM-dd")}</span>
                                <div>
                                </div>
                            </div>
                        </section>
                    </li>
                    </#list>
                </ul>
            </div>
        </div>
    </div>
</div>
<script src="/static/plugins/jquery/jquery.min.js"></script>
<script>
    $('#btn-push').click(function () {
        $.ajax({
            type: 'POST',
            url: '/newComment',
            async: false,
            data:{
                'postId' : $('input[name=postId]').val(),
                'commentContent' : $('textarea[name=commentContent]').val(),
                'commentAuthor' : $('input[name=commentAuthor]').val(),
                'commentAuthorEmail' : $('input[name=commentAuthorEmail]').val(),
                'commentAuthorUrl' : $('input[name=commentAuthorUrl]').val()
            },
            success: function (data) {
                if(data=="success"){
                    window.location.reload();
                }
            }
        });
    });
</script>