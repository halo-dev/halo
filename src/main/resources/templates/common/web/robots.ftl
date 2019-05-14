<#if options.spider_disabled!false>
User-agent: /
Disallow: /
<#else>
User-agent: *
Disallow: /admin/
Sitemap: ${options.blog_url!}/sitemap.xml
Sitemap: ${options.blog_url!}/sitemap.html
</#if>