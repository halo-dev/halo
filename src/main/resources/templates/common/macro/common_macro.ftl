<#ftl strip_whitespace=true>

<#-- 统计代码 -->
<#macro statistics>
    ${options.statistics_code!}
</#macro>

<#-- 页脚信息 -->
<#macro footer_info>
    ${options.blog_footer_info!}
</#macro>

<#-- favicon -->
<#macro favicon>
    <#if options.blog_favicon??>
        <link rel="shortcut icon" type="images/x-icon" href="${options.blog_favicon}">
    </#if>
</#macro>

<#-- 站点验证代码 -->
<#macro verification>
    <#if options.blog_verification_google??>
        <meta name="google-site-verification" content="${options.blog_verification_google}" />
    </#if>
    <#if options.blog_verification_bing??>
        <meta name="msvalidate.01" content="${options.blog_verification_bing}" />
    </#if>
    <#if options.blog_verification_baidu??>
        <meta name="baidu-site-verification" content="${options.blog_verification_baidu}" />
    </#if>
    <#if options.blog_verification_qihu??>
        <meta name="360-site-verification" content="${options.blog_verification_qihu}" />
    </#if>
</#macro>

<#-- 时间格式化 几...前 -->
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
