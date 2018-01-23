<#include "module/macro.ftl">
<@layout title="${post.postTitle?if_exists} | ${options.site_title?default('Material')}" keywords="${options.seo_keywords?default('Material')}" description="${options.seo_desc?default('Material')}">
    <!-- Post Module -->
    <div class="material-post_container">
        <div class="material-post mdl-grid">
            <div class="mdl-card mdl-shadow--4dp mdl-cell mdl-cell--12-col">

                <!-- Post Header(Thumbnail & Title) -->
                <#include "_partial/post-header.ftl">

                <#if options.theme_material_scheme?if_exists == "Paradox">
                <!-- Paradox Post Info -->
                <#include "_partial/Paradox-post-info.ftl">
                </#if>

                <!-- Post Content -->
                <#include "_partial/post-content.ftl">

                <#if options.theme_material_scheme?if_exists == "Isolation">
                <#include "_partial/Isolation-post-info.ftl">
                </#if>

                <#include "_partial/comment.ftl">
            </div>
            <!-- Post Prev & Next Nav -->
            <#include "_partial/post-nav.ftl">
        </div>
    </div>
</@layout>