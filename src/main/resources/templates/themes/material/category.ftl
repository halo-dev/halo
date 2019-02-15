<#include "module/macro.ftl">
<@layout title="分类目录：${category.cateName} | ${options.blog_title!'Material'}" keywords="${options.seo_keywords!'Material'}}" description="${options.seo_desc!'Material'}}">
    <!-- Index Module -->
    <div class="material-index mdl-grid">
        <div class="locate-thumbnail-symbol"></div>
        <!-- Pin on top -->

        <!-- Normal Post -->
        <#if (options.theme_material_scheme!'Paradox') == "Paradox">
        <!-- Paradox Thumbnail -->
            <#include "_partial/Paradox-post_entry.ftl">
        <#else>
        <!-- Isolation Thumbnail -->
            <#include "_partial/Isolation-post_entry.ftl">
        </#if>
        <#include "_partial/index-nav.ftl">
        <@nav url="${options.blog_url!}/categories/${category.cateUrl}/"></@nav>
        <#if (options.theme_material_scheme!'Paradox') == "Paradox">
            <#include "_partial/Paradox-post_entry-thumbnail.ftl">
        </#if>
    </div>
</@layout>
