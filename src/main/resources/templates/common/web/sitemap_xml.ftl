<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
    <#if posts?? && posts?size gt 0>
        <#list posts as post>
            <url>
                <loc><#if !globalAbsolutePathEnabled!true>${blog_url!}</#if>${post.fullPath!}</loc>
                <lastmod>${post.createTime?iso_local}</lastmod>
            </url>
        </#list>
    </#if>
</urlset>