<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
    <#if posts?? && posts?size gt 0>
        <#list posts as post>
            <url>
                <loc>${options.blog_url!}/archives/${post.postUrl!}</loc>
                <lastmod>${post.postDate?iso_local}</lastmod>
            </url>
        </#list>
    </#if>
</urlset>