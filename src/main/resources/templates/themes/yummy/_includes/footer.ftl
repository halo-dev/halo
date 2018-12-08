<#include "/common/macro/common_macro.ftl">
<#macro footer>
<footer class="container">

    <div class="site-footer">

        <div class="copyright pull-left">
            <!-- 请不要更改这一行 方便其他人知道模板的来源 谢谢 -->
            <!-- Please keep this line to let others know where this theme comes from. Thank you :D -->
            Power by <a href="https://github.com/DONGChuan/Yummy-Jekyll">Yummy </a><a href="https://github.com/ruibaby/halo">Halo</a>
        </div>

        <a href="${options.yummy_general_github_url?default('#')}" target="_blank" aria-label="view source code">
            <span class="mega-octicon octicon-mark-github" title="GitHub"></span>
        </a>

        <div class="pull-right">
            <a href="javascript:window.scrollTo(0,0)" >TOP</a>
        </div>

    </div>

    <!-- Third-Party JS -->
    <script type="text/javascript" src="/${themeName}/bower_components/geopattern/js/geopattern.min.js"></script>

    <!-- My JS -->
    <script type="text/javascript" src="/${themeName}/assets/js/script.js"></script>

    <#nested />

    <!-- Google Analytics -->
    <div style="display:none">
        <@statistics></@statistics>
    </div>

</footer>
</#macro>
