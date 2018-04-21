<#if post.categories?size gt 0>
    <ul class="article-category-list">
	<#list post.categories as cate>
        <li class="article-category-list-item">
            <a class="article-category-list-link" href="/categories/${cate.cateUrl}/">${cate.cateName}</a>
        </li>
	</#list>
    </ul>
</#if>