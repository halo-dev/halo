<#if options.seo_spider_disabled!false>
User-agent: *
Disallow: /
<#else>
User-agent: *
Sitemap: ${context!}/sitemap.xml
Sitemap: ${context!}/sitemap.html
</#if>