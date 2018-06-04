<div class="emailpaged" style="background: #fff;">
    <div class="emailcontent" style="width:100%;max-width:720px;text-align: left;margin: 0 auto;padding-top: 20px;padding-bottom: 80px">
        <div class="emailtitle" style="border-radius: 5px;border:1px solid #eee;overflow: hidden;">
            <h1 style="color:#fff;background: #3798e8;line-height:70px;font-size:24px;font-weight:normal;padding-left:40px;margin:0">
                您有新的备份，请按需下载附件。
            </h1>
            <div class="emailtext" style="background:#fff;padding:20px 32px 40px;">
                备份详情：<br />
                文件名：${fileName}<br />
                备份时间：${createAt?string("yyyy-MM-dd HH:mm")}<br />
                文件大小：${size}
            </div>
        </div>
    </div>
</div>