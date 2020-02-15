<?xml version="1.0" encoding="utf-8"?>
<rss version="2.0">
    <channel>
        <title>${blog_title!}</title>
        <link>${blog_url!}</link>
        <#if user.description??>
            <description>${user.description!}</description>
        </#if>
        <language>zh-CN</language>
        <generator>Halo ${version!}</generator>
        <#if posts?? && posts?size gt 0>
            <#list posts as post>
                <item>
                    <title>
                        <![CDATA[
                            ${post.title!}
                        ]]>
                    </title>
                    <link>${post.fullPath!}</link>
                    <description>
                        <![CDATA[
                            <#if (options.rss_content_type!'full') == 'full'>
                                ${post.formatContent!}
                            <#else>
                                ${post.summary!}
                            </#if>
                        ]]>
                    </description>
                    <pubDate>${post.createTime?iso_local}</pubDate>
                </item>
            </#list>
        </#if>
    </channel>
</rss>