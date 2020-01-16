<div style="text-align:center">
    <img src="${user.avatar!}" width="100" height="100" alt="${user.nickname!}">
    <h3>${options.blog_title!}</h3>
    <h4>
        <a href="${context!}" target="_blank">${context!}</a>
    </h4>
</div>

---

<@postTag method="archiveYear">
<#list archives as archive>
## ${archive.year?c}
<#list archive.posts?sort_by("createTime")?reverse as post>
- <a href="${context!}/archives/${post.url!}" title="${post.title!}" target="_blank">${post.title!}</a>
</#list>
</#list>
</@postTag>

## 分类目录
<@categoryTag method="list">
<#list categories as category>
- <a href="${context!}/categories/${category.slugName!}" target="_blank">${category.name!}</a>
</#list>
</@categoryTag>

## 标签
<@tagTag method="list">
<#list tags as tag>
- <a href="${context!}/tags/${tag.slugName!}" target="_blank">${tag.name!}</a>
</#list>
</@tagTag>