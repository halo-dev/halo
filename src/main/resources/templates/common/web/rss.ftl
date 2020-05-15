<?xml version="1.0" encoding="utf-8"?>
<rss version="2.0">
    <channel>
        <#if category??>
            <title>分类：${category.name!} - ${blog_title!}</title>
        <#else>
            <title>${blog_title!}</title>
        </#if>
        <#if category??>
            <link>${category.fullPath!}</link>
        <#else>
            <link>${blog_url!}</link>
        </#if>
        <#if category??>
            <#if category.description?? && category.description!=''>
                <description>${category.description!}</description>
            </#if>
        <#else>
            <#if user.description?? && user.description!=''>
                <description>${user.description!}</description>
            </#if>
        </#if>
        <language>zh-CN</language>
        <generator>Halo ${version!}</generator>
        <#if posts?? && posts?size gt 0>
            <#list posts as post>
                <item>
                    <title>
                        <![CDATA[${post.title!}]]>
                    </title>
                    <link><#if !globalAbsolutePathEnabled!true>${blog_url!}</#if>${post.fullPath!}</link>
                    <description>
                        <#if (options.rss_content_type!'full') == 'full'>
                            <![CDATA[${post.formatContent!}]]>
                        <#else>
                            <![CDATA[${post.summary!}]]>
                        </#if>
                    </description>
                    <pubDate>${post.createTime?iso_local}</pubDate>
                </item>
            </#list>
        </#if>
    </channel>
</rss>