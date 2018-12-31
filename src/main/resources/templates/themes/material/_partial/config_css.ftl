<!-- Config CSS -->

<!-- Other Styles -->
<style>
    body, html {
        font-family: Roboto, "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
        overflow-x: hidden !important;
    }

    code {
        font-family: Consolas, Monaco, 'Andale Mono', 'Ubuntu Mono', monospace;
    }

    a {
        color: ${options.theme_material_uiux_hyperlink_color!'#00838F'};
    }

    .mdl-card__media,
    #search-label,
    #search-form-label:after,
    #scheme-Paradox .hot_tags-count,
    #scheme-Paradox .sidebar_archives-count,
    #scheme-Paradox .sidebar-colored .sidebar-header,
    #scheme-Paradox .sidebar-colored .sidebar-badge{
        background-color: ${options.theme_material_uiux_theme_color!'#0097A7'} !important;
    }

    /* Sidebar User Drop Down Menu Text Color */
    #scheme-Paradox .sidebar-colored .sidebar-nav>.dropdown>.dropdown-menu>li>a:hover,
    #scheme-Paradox .sidebar-colored .sidebar-nav>.dropdown>.dropdown-menu>li>a:focus {
        color: ${options.theme_material_uiux_theme_color!'#0097A7'} !important;
    }

    #post_entry-right-info,
    .sidebar-colored .sidebar-nav li:hover > a,
    .sidebar-colored .sidebar-nav li:hover > a i,
    .sidebar-colored .sidebar-nav li > a:hover,
    .sidebar-colored .sidebar-nav li > a:hover i,
    .sidebar-colored .sidebar-nav li > a:focus i,
    .sidebar-colored .sidebar-nav > .open > a,
    .sidebar-colored .sidebar-nav > .open > a:hover,
    .sidebar-colored .sidebar-nav > .open > a:focus,
    #ds-reset #ds-ctx .ds-ctx-entry .ds-ctx-head a {
        color: ${options.theme_material_uiux_theme_color!'#0097A7'} !important;
    }

    .toTop {
        background: ${options.theme_material_uiux_button_color!'#757575'} !important;
    }

    .material-layout .material-post>.material-nav,
    .material-layout .material-index>.material-nav,
    .material-nav a {
        color: ${options.theme_material_uiux_button_color!'#757575'};
    }

    #scheme-Paradox .MD-burger-layer {
        background-color: ${options.theme_material_uiux_button_color!'#757575'};
    }

    #scheme-Paradox #post-toc-trigger-btn {
        color: ${options.theme_material_uiux_button_color!'#757575'};
    }

    .post-toc a:hover {
        color: ${options.theme_material_uiux_hyperlink_color!'#00838F'};
        text-decoration: underline;
    }

</style>


<!-- Theme Background Related-->
<#if (options.theme_material_background_bing!'false')=="true">
    <style>
        body {
            background-color: ${options.theme_material_background_purecolor!'#F5F5F5'};
        }

        /* blog_info bottom background */
        #scheme-Paradox .material-layout .something-else .mdl-card__supporting-text {
            background-color: #fff;
        }
    </style>
<#elseif !options.theme_material_background_bgimg??>
    <style>
        body{
            background-color: ${options.theme_material_background_purecolor!'#F5F5F5'};
        }

        /* blog_info bottom background */
        #scheme-Paradox .material-layout .something-else .mdl-card__supporting-text{
            background-color: #fff;
        }
    </style>
<#else >
    <style>
        body{
            background-image: url(${options.theme_material_background_bgimg!});
        }
    </style>
</#if>



<!-- Fade Effect -->

<#if (options.theme_material_other_js_fade!'true')=='true'>
<style>
    .fade {
        transition: all ${options.theme_material_uiux_nprogress_buffer!'800'}ms linear;
        -webkit-transform: translate3d(0,0,0);
        -moz-transform: translate3d(0,0,0);
        -ms-transform: translate3d(0,0,0);
        -o-transform: translate3d(0,0,0);
        transform: translate3d(0,0,0);
        opacity: 1;
    }

    .fade.out{
        opacity: 0;
    }
</style>
</#if>

<!-- Import Font -->
<#include "config_font.ftl">
