<meta http-equiv="x-dns-prefetch-control" content="on">
<!--
<% if(theme.vendors.materialcdn) { %>
    <link rel="dns-prefetch" href="<%= theme.vendors.materialcdn %>"/>
<% } %>
-->
<#if (options.comment_system?if_exists)=='valine'>
    <link rel="dns-prefetch" href="https://cdn1.lncld.net"/>
</#if>
<!--
<% if(theme.busuanzi.enable === true) { %>
    <link rel="dns-prefetch" href="https://busuanzi.ibruce.info"/>
<% } %>
-->
<#if (options.comment_system?if_exists)=='changyan'>
    <link rel="dns-prefetch" href="https://changyan.sohu.com"/>
</#if>
<#if (options.comment_system?if_exists)=='livere'>
        <link rel="dns-prefetch" href="https://cdn-city.livere.com"/>
</#if>
<!--
<% if(theme.analytics.use === "baidu") { %>
        <link rel="dns-prefetch" href="https://hm.baidu.com"/>
<% } %>
<% if(theme.analytics.use === "google") { %>
        <link rel="dns-prefetch" href="https://www.google-analytics.com"/>
<% } %>
<% if(theme.analytics.use === "cnzz") { %>
        <link rel="dns-prefetch" href="https://s95.cnzz.com"/>
<% } %>
<% if(theme.fonts.host === "google") { %>
    <link rel="dns-prefetch" href="https://fonts.googleapis.com"/>
<% } %>
<% if(theme.fonts.host === "baomitu") { %>
    <link rel="dns-prefetch" href="https://lib.baomitu.com"/>
<% } %>
-->
<link rel="dns-prefetch" href="<%= theme.fonts.custom_font_host %>"/>
<!--
<% if(theme.qrcode.use === "online" && theme.qrcode.enable === "true") { %>
    <% if(config.language === "zh-CN") { %>
        <link rel="dns-prefetch" href="https://pan.baidu.com"/>
    <% } else { %>
        <link rel="dns-prefetch" href="https://chart.googleapis.com"/>
    <% } %>
<% } %>
-->