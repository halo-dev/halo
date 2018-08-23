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
