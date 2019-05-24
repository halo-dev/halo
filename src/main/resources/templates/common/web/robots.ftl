<#if options.spider_disabled!false>
User-agent: /
Disallow: /
<#else>
User-agent: *
Disallow: /admin/
Sitemap: ${context!}/sitemap.xml
Sitemap: ${context!}/sitemap.html
</#if>