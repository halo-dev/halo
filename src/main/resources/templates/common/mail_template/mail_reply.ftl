<div class="emailpaged" style="background: #fff;">
    <div class="emailcontent" style="width:100%;max-width:720px;text-align: left;margin: 0 auto;padding-top: 20px;padding-bottom: 80px">
        <div class="emailtitle" style="border-radius: 5px;border:1px solid #eee;overflow: hidden;">
            <h1 style="color:#fff;background: #3798e8;line-height:70px;font-size:24px;font-weight:normal;padding-left:40px;margin:0">
                您在 ${blog_title!} 上的留言有回复啦！
            </h1>
            <div class="emailtext" style="background:#fff;padding:20px 32px 40px;">

                <p style="color: #6e6e6e;font-size:13px;line-height:24px;">${baseAuthor!}, 您好!</p>
                <p style="color: #6e6e6e;font-size:13px;line-height:24px;">您在《${pageTitle!}》的留言:
                    <br />
                <p style="color: #6e6e6e;font-size:13px;line-height:24px;padding:10px 20px;background:#f8f8f8;margin:0">${baseContent!}</p>
                <p style="color: #6e6e6e;font-size:13px;line-height:24px;">${replyAuthor!} 给您的回复:
                    <br />
                <p style="color: #6e6e6e;font-size:13px;line-height:24px;padding:10px 20px;background:#f8f8f8;margin:0">${replyContent!}</p>
                <p style="color: #6e6e6e;font-size:13px;line-height:24px;">您可以点击
                    <a href="${pageFullPath!}">查看完整内容</a>
                </p>
                <p style="color: #6e6e6e;font-size:13px;line-height:24px;">欢迎再度光临
                    <a href="${blog_url!}">${blog_title!}</a>
                </p>

                <p style="color: #6e6e6e;font-size:13px;line-height:24px;">(此邮件由系统自动发出, 请勿回复。如有打扰，请见谅。)</p>
            </div>
            <p style="color: #6e6e6e;font-size:13px;line-height:24px;text-align:right;padding:0 32px">邮件发自：
                <a href="${blog_url!}" style="color:#51a0e3;text-decoration:none">${blog_title!}</a>
            </p>
        </div>
    </div>
</div>