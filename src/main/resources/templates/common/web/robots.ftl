<#if options.seo_spider_disabled!false>
User-agent: *
Disallow: /
<#else>
User-agent: *
Sitemap: ${sitemap_xml_url!}
Sitemap: ${sitemap_html_url!}
</#if>