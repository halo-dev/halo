<!-- Import JS File -->
<script>lsloader.load("lazyload_js","/material/source/js/lazyload.min.js?1BcfzuNXqV+ntF6gq+5X3Q==", true)</script>
<script>lsloader.load("js_js","/material/source/js/js.min.js?V/53wGualMuiPM3xoetD5Q==", true)</script>

<#include "../_widget/nprogress.ftl">
<#if options.theme_material_other_js_smoothscroll?default('true')=='true'>
    <script>lsloader.load("sm_js","/material/source/js/smoothscroll.js?lOy/ACj5suSNi7ZVFVbpFQ==", true)</script>
</#if>
<#include "footer-option.ftl">
<!-- UC Browser Compatible -->
<script>
    var agent = navigator.userAgent.toLowerCase();
    if(agent.indexOf('ucbrowser')>0) {
        document.write('<%- css("css/uc") %>');
        alert('由于 UC 浏览器使用极旧的内核，而本网站使用了一些新的特性。\n为了您能更好的浏览，推荐使用 Chrome 或 Firefox 浏览器。');
    }
</script>

<!-- Import prettify js  -->

<script>
    lsloader.load("prettify_js", "/material/source/js/prettify.min.js?WN07fivHQSMKWy7BmHBB6w==", !0)
</script>
<!--
<% if (theme.hanabi.enable) { %>
    <% if(theme.vendors.materialcdn) { %>
        <%- jsLsload({path:(theme.vendors.materialcdn + '/js/hanabi-browser-bundle.js'),key:'hanabi'}) %>
    <% } else { %>
        <%- jsLsload({path:('js/hanabi-browser-bundle.js'),key:'hanabi'}) %>
    <% } %>
<% } %>
-->
<!-- Window Load -->
<!-- add class for prettify -->
<script type="text/ls-javascript" id="window-load">
    $(window).on('load', function() {
        // Post_Toc parent position fixed
        $('.post-toc-wrap').parent('.mdl-menu__container').css('position', 'fixed');
    });
</script>

<!-- MathJax Load-->
<!--
<% if (page.mathjax) { %>
    <%- partial('_widget/mathjax') %>
<% } %>
-->

<!-- Bing Background -->
<#if options.theme_material_background_bing?default('false')=="true">
<script type="text/ls-javascript" id="Bing-Background-script">
    queue.offer(function(){
        $('body').attr('data-original', 'https://api.i-meto.com/bing');
    });
</script>
</#if>

<script type="text/ls-javascript" id="lazy-load">
    // Offer LazyLoad
    queue.offer(function(){
        $('.lazy').lazyload({
            effect : 'show'
        });
    });

    // Start Queue
    $(document).ready(function(){
        setInterval(function(){
            queue.execNext();
        },200);
    });
</script>

<!-- Custom Footer -->
<!--
<% if (site.data.footer) { %>
    <% for (var i in site.data.footer) { %>
        <%- site.data.footer[i] %>
    <% } %>
<% } %>
-->
<script>
    (function(){
        var scriptList = document.querySelectorAll('script[type="text/ls-javascript"]')

        for (var i = 0; i < scriptList.length; ++i) {
            var item = scriptList[i];
            lsloader.runInlineScript(item.id,item.id);
        }
    })()
    console.log('\n %c © Material Theme | Version: 1.5.2 | https://github.com/viosey/hexo-theme-material %c \n', 'color:#455a64;background:#e0e0e0;padding:5px 0;border-top-left-radius:5px;border-bottom-left-radius:5px;', 'color:#455a64;background:#e0e0e0;padding:5px 0;border-top-right-radius:5px;border-bottom-right-radius:5px;');
</script>
