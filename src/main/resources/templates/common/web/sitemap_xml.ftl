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
    <@categoryTag method="list">
        <#if categories?? && categories?size gt 0>
            <#list categories as category>
                <url>
                    <loc><#if !globalAbsolutePathEnabled!true>${blog_url!}</#if>${category.fullPath!}</loc>
                    <lastmod>${category.createTime?iso_local}</lastmod>
                </url>
            </#list>
        </#if>
    </@categoryTag>
    <@tagTag method="list">
        <#if tags?? && tags?size gt 0>
            <#list tags as tag>
                <url>
                    <loc><#if !globalAbsolutePathEnabled!true>${blog_url!}</#if>${tag.fullPath!}</loc>
                    <lastmod>${tag.createTime?iso_local}</lastmod>
                </url>
            </#list>
        </#if>
    </@tagTag>
</urlset>