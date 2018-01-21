<#list archives as archive>
    <span>${archive.year}年${archive.month}月</span><br><br>
    <#list archive.posts as post>
        <a href="#">${post.postTitle}</a><br>
    </#list>
    <br><br><br><br>
</#list>