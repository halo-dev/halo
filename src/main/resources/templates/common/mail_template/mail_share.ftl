<style>
    .emailpaged {
        font-family: "Courier New", -apple-system, BlinkMacSystemFont, "Segoe UI", "Roboto", "Helvetica Neue", Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol";
    }
</style>
<div class="emailpaged" style="background: #fff;">
    <div class="emailcontent" style="width:100%;text-align: left;margin: 0 auto;padding-top: 20px;padding-bottom: 80px">
        <div class="emailtitle" style="border-radius: 5px;overflow: hidden;">
            <h1 style="line-height:30px;font-size:24px;font-weight:normal;padding-left:40px;margin:0">
                嗨！您的朋友
                <a href="${blogBasePath!}">${nickname!}</a>
                有新的文章<br>
            </h1>
            <div class="emailtext" style="background:#fff;padding:20px 32px 40px;">
                <div style="color: #6e6e6e;font-size:13px;padding:10px 20px;background:#f8f8f8;margin:0">
                    <h4 style="font-weight:700!important;color:#212529!important;margin: 0.33em 0;font-size:18px;">《${pageTitle!}》</h4>
                    <p style="font-size:16px;">
                        &nbsp;&nbsp;&nbsp;&nbsp;${pageContent!}
                    </p>
                </div>
                <br/>
                <p style="color: #6e6e6e;font-size:13px;line-height:24px;">
                    您可以点击<a href="${pageFullPath!}" style="margin:0 2px;">查看完整内容</a>
                </p>
            </div>
        </div>
        如不想收到此类邮件,<a style="margin:0 2px;">点击退订</a>
    </div>
</div>