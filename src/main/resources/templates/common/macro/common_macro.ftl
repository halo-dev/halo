<#ftl strip_whitespace=true>

<#-- 统计代码 -->
<#macro statistics>
    ${options.statistics_code?if_exists}
</#macro>

<#-- 页脚信息 -->
<#macro footer_info>
    ${options.blog_footer_info?if_exists}
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