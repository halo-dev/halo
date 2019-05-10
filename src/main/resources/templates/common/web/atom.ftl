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
        <atom:link href="${options.blog_url!}/atom.xml" rel="self" type="application/rss+xml"/>
        <link>${options.blog_url!}</link>
        <description>${user.description!}</description>
        <language>zh-CN</language>
        <sy:updatePeriod>hourly</sy:updatePeriod>
        <sy:updateFrequency>1</sy:updateFrequency>
        <generator>https://github.com/halo-dev/halo</generator>
    </channel>
    <#if posts?? && posts?size gt 0>
        <#list posts as post>
            <item>
                <title>${post.title!}</title>
                <link>${options.blog_url!}/archives/${post.url!}</link>
                <comments>${options.blog_url!}/archives/${post.url!}#comments</comments>
                <pubDate>${post.createTime!}</pubDate>
                <dc:creator><![CDATA[${user.nickname!}]]></dc:creator>

                <#if post.categories?? && post.categories?size gt 0>
                    <#list post.categories as cate>
                        <category><![CDATA[${cate.name!}]]></category>
                    </#list>
                </#if>
                <description>
                    <![CDATA[
                        ${post.summary!}
                    ]]>
                </description>
                <content:encoded>
                    <![CDATA[
                        ${post.summary!}
                    ]]>
                </content:encoded>
                <slash:comments>${post.commentCount!0}</slash:comments>
            </item>
        </#list>
    </#if>
</rss>
