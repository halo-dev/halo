<#ftl strip_whitespace=true>

<#-- statistics_code -->
<#macro statistics>
    ${options.blog_statistics_code!}
</#macro>

<#-- footer info -->
<#macro footer_info>
    ${options.blog_footer_info!}
</#macro>

<#macro custom_head>
    ${options.blog_custom_head!}
</#macro>

<#macro custom_content_head>
    <#if is_post?? || is_sheet??>
        ${options.blog_custom_content_head!}
    </#if>
</#macro>

<#-- favicon -->
<#macro favicon>
    <#if options.blog_favicon?? && options.blog_favicon!=''>
        <link rel="shortcut icon" type="images/x-icon" href="${options.blog_favicon!}">
    </#if>
</#macro>

<#-- time ago -->
<#macro timeline datetime=.now>
    <#assign ct = (.now?long-datetime?long)/1000>
    <#if ct gte 31104000>${(ct/31104000)?int} 年前
        <#t><#elseif ct gte 2592000>${(ct/2592000)?int} 个月前
        <#t><#elseif ct gte 86400*2>${(ct/86400)?int} 天前
        <#t><#elseif ct gte 86400>昨天
        <#t><#elseif ct gte 3600>${(ct/3600)?int} 小时前
        <#t><#elseif ct gte 60>${(ct/60)?int} 分钟前
        <#t><#elseif ct gt 0>${ct?int} 秒前
        <#t><#else>刚刚
    </#if>
</#macro>

<#-- global head -->
<#macro head>
    <#if options.seo_spider_disabled!false>
        <meta name="robots" content="none">
    </#if>
    <meta name="generator" content="Halo ${version!}"/>
    <@favicon />
    <@custom_head />
    <@custom_content_head />
</#macro>

<#-- global footer -->
<#macro footer>
    <@footer_info />
    <@statistics />
</#macro>

<#-- comment module -->
<#macro comment target,type>
    <#if !post.disallowComment!false>
        <script src="https://unpkg.com/vue@2.6.14/dist/vue.min.js"></script>
        <script src="${options.comment_internal_plugin_js!'https://unpkg.com/halo-comment@latest/dist/halo-comment.min.js'}"></script>
        <halo-comment id="${target.id}" type="${type}"/>
    </#if>
</#macro>