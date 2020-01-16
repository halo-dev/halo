<?xml version="1.0" encoding="UTF-8"?>
<rss xmlns:content="http://purl.org/rss/1.0/modules/content/" version="2.0">
    <channel>
        <title>${options.blog_title!}</title>
        <link>${context!}</link>
        <#if user.description??>
            <description>${user.description!}</description>
        </#if>
        <language>zh-CN</language>
        <#if posts?? && posts?size gt 0>
            <#list posts as post>
                <item>
                    <title>
                        <![CDATA[
                            ${post.title!}
                        ]]>
                    </title>
                    <link>${options.blog_url}/archives/${post.url!}</link>
                    <content:encoded>
                        <![CDATA[
                            <#if (options.rss_content_type!'full') == 'full'>
                                ${post.formatContent!}
                            <#else>
                                ${post.summary!}
                            </#if>
                        ]]>
                    </content:encoded>
                    <pubDate>${post.createTime}</pubDate>
                </item>
            </#list>
        </#if>
    </channel>
</rss>