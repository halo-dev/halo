<?xml version="1.0" encoding="utf-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
    <#if category??>
        <title type="text">分类：${category.name!} - ${blog_title!}</title>
    <#else>
        <title type="text">${blog_title!}</title>
    </#if>
    <#if category??>
        <#if category.description?? && category.description!=''>
            <subtitle type="text">${category.description!}</subtitle>
        </#if>
    <#else>
        <#if user.description?? && user.description!=''>
            <subtitle type="text">${user.description!}</subtitle>
        </#if>
    </#if>
    <updated>${lastModified?iso_local}</updated>
    <#if category??>
        <id>${category.fullPath!}</id>
    <#else>
        <id>${blog_url!}</id>
    </#if>
    <#if category??>
        <link rel="alternate" type="text/html" href="${category.fullPath!}" />
        <link rel="self" type="application/atom+xml" href="${blog_url!}/feed/categories/${category.slug}.xml" />
    <#else>
        <link rel="alternate" type="text/html" href="${blog_url!}" />
        <link rel="self" type="application/atom+xml" href="${atom_url!}" />
    </#if>
    <rights>Copyright © ${.now?string('yyyy')}, ${blog_title!}</rights>
    <generator uri="https://halo.run/" version="${version!}">Halo</generator>
    <#if posts?? && posts?size gt 0>
        <#list posts as post>
            <entry>
                <title><![CDATA[${post.title!}]]></title>
                <link rel="alternate" type="text/html" href="<#if !globalAbsolutePathEnabled!true>${blog_url!}</#if>${post.fullPath!}" />
                <id>tag:${blog_url!},${post.createTime?string('yyyy-MM-dd')}:${post.slug!}</id>
                <published>${post.createTime?iso_local}</published>
                <updated>${post.editTime?iso_local}</updated>
                <author>
                    <name>${user.nickname!}</name>
                    <uri>${blog_url!}</uri>
                </author>
                <content type="html">
                    <#if (options.rss_content_type!'full') == 'full'>
                        <![CDATA[${post.formatContent!}]]>
                    <#else>
                        <![CDATA[${post.summary!}]]>
                    </#if>
                </content>
            </entry>
        </#list>
    </#if>
</feed>
