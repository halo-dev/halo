<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0"
     xmlns:content="http://purl.org/rss/1.0/modules/content/"
     xmlns:wfw="http://wellformedweb.org/CommentAPI/"
     xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:atom="http://www.w3.org/2005/Atom"
     xmlns:sy="http://purl.org/rss/1.0/modules/syndication/"
     xmlns:slash="http://purl.org/rss/1.0/modules/slash/">
    <channel>
        <title>${options.blog_title!}</title>
        <atom:link href="${context!}/atom.xml" rel="self" type="application/rss+xml"/>
        <link>${context!}</link>
        <description>${user.description!}</description>
        <language>zh-CN</language>
        <sy:updatePeriod>hourly</sy:updatePeriod>
        <sy:updateFrequency>1</sy:updateFrequency>
        <generator>https://halo.run</generator>
    </channel>
    <#if posts?? && posts?size gt 0>
        <#list posts as post>
            <item>
                <title><![CDATA[${post.title!}]]></title>
                <link>${context!}/archives/${post.url!}</link>
                <comments>${context!}/archives/${post.url!}#comments</comments>
                <pubDate>${post.createTime!}</pubDate>
                <dc:creator><![CDATA[${user.nickname!}]]></dc:creator>

                <#if post.categories?? && post.categories?size gt 0>
                    <#list post.categories as category>
                        <category><![CDATA[${category.name!}]]></category>
                    </#list>
                </#if>
                <description>
                    <![CDATA[
                        ${post.summary!}
                    ]]>
                </description>
                <content:encoded>
                    <![CDATA[
                        <#if (options.rss_content_type!'full') == 'full'>
                            ${post.formatContent!}
                        <#else>
                            ${post.summary!}
                        </#if>
                    ]]>
                </content:encoded>
            </item>
        </#list>
    </#if>
</rss>
