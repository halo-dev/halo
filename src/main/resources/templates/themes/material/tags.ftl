<#include "module/macro.ftl">
<@layout title="标签云 | ${options.site_title?if_exists}" keywords="${options.seo_keywords?if_exists}" description="${options.seo_desc?if_exists}">
    <#include "_widget/page-tagcloud.ftl">
</@layout>