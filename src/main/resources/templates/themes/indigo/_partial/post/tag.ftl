<ul class="article-tag-list">
    <#list post.tags as tag>
        <li class="article-tag-list-item">
            <a class="article-tag-list-link" href="tags/${tag.tagUrl}/">${tag.tagName}</a>
        </li>
    </#list>
</ul>