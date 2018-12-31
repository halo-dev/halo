<#macro layout title="" keywords="" description="">
<!DOCTYPE html>
<html  style="display: none;" lang="zh">
    <#include "../_partial/head.ftl">
    <@head title="${title}" keywords="${keywords}" description="${description}"></@head>
    <body id="scheme-${options.theme_material_scheme!'Paradox'}" class="lazy">
        <div class="material-layout  mdl-js-layout has-drawer is-upgraded">
            <#if (options.theme_material_scheme!'Paradox') == "Isolation">
                <#include "../_partial/isolate_info.ftl">
            </#if>
            <!-- Main Container -->
            <main class="material-layout__content" id="main">

                <!-- Top Anchor -->
                <div id="top"></div>

                <#if (options.theme_material_scheme!'Paradox') == "Paradox">
                <!-- Hamburger Button -->
                <button class="MD-burger-icon sidebar-toggle">
                    <span class="MD-burger-layer"></span>
                </button>
                </#if>

                <!--body-->
                <#nested >
                <#if (options.theme_material_scheme!'Paradox') == "Paradox">
                <#include "../_partial/sidebar.ftl">
                <!-- Footer Top Button -->
                <#include "../_partial/footer_top.ftl">
                </#if>
                <#include "../_partial/footer.ftl">
                <#include "../_partial/import_js.ftl">
            </main>
        </div>
    </body>
</html>
</#macro>
