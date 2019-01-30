<#include "module/macro.ftl">
<@layout title="${post.postTitle!} | ${options.blog_title!'Material'}" keywords="${options.seo_keywords!'Material'},${tagWords}" description="${post.postSummary!}">
    <!-- Post Module -->
    <div class="material-post_container">
        <div class="material-post mdl-grid">
            <div class="mdl-card mdl-shadow--4dp mdl-cell mdl-cell--12-col">

                <!-- Post Header(Thumbnail & Title) -->
                <#include "_partial/post-header.ftl">

                <#if (options.theme_material_scheme!'Paradox') == "Paradox">
                <!-- Paradox Post Info -->
                <#include "_partial/Paradox-post-info.ftl">
                </#if>

                <!-- Post Content -->
                <#include "_partial/post-content.ftl">

                <#if (options.theme_material_scheme!'Paradox') == "Isolation">
                <#include "_partial/Isolation-post-info.ftl">
                </#if>

                <#include "_partial/comment.ftl">
            </div>
            <!-- Post Prev & Next Nav -->
            <#include "_partial/post-nav.ftl">
        </div>
    </div>
</@layout>
