<?xml version="1.0" encoding="utf-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
    <title type="text">${options.blog_title!}</title>
    <#if user.description??>
        <subtitle type="text">${user.description!}</subtitle>
    </#if>
    <updated>${.now?iso_local}</updated>
    <id>${options.blog_url!}</id>
    <link rel="alternate" type="text/html" href="${options.blog_url!}" />
    <link rel="self" type="application/atom+xml" href="${options.blog_url!}/atom.xml" />
    <rights>Copyright © ${.now?string('yyyy')}, ${options.blog_title!}</rights>
    <generator uri="https://halo.run/" version="${version!}">Halo</generator>
    <#if posts?? && posts?size gt 0>
        <#list posts as post>
            <entry>
                <title><![CDATA[${post.title!}]]></title>
                <link rel="alternate" type="text/html" href="${post.fullPath!}" />
                <id>tag:${options.blog_url!},${post.createTime?string('yyyy-MM-dd')}:${post.url!}</id>
                <published>${post.createTime?iso_local}</published>
                <updated>${post.editTime?iso_local}</updated>
                <author>
                    <name>${user.nickname!}</name>
                    <uri>${options.blog_url!}</uri>
                </author>
                <content type="html" xml:base="${options.blog_url!}" xml:lang="en">
                    <![CDATA[
                        <#if (options.rss_content_type!'full') == 'full'>
                            ${post.formatContent!}
                        <#else>
                            ${post.summary!}
                        </#if>
                    ]]>
                </content>
            </entry>
        </#list>
    </#if>
</feed>
