<!--Footer-->
<footer class="mdl-mini-footer" id="bottom">
    <#if (options.theme_material_scheme!'Paradox') == "Paradox">
    <!-- Paradox Footer Left Section -->
    <#include "footer-left.ftl">

    <!--Copyright-->
    <div id="copyright">
        Copyright&nbsp;©&nbsp;2016&nbsp;-<script type="text/javascript">var fd = new Date();document.write("&nbsp;" + fd.getFullYear() + "&nbsp;");</script>${options.blog_title!}
        <br>
        ${options.blog_footer!}
    </div>

    <!-- Paradox Footer Right Section -->

    <!--
    I am glad you use this theme, the development is no so easy, I hope you can keep the copyright.
    It will not impact the appearance and can give developers a lot of support :)

    很高兴您使用该主题，开发不易，希望您可以保留一下版权声明。
    它不会影响美观并可以给开发者很大的支持。 :)
    -->

    <div class="mdl-mini-footer--right-section">
        <div>
            <div class="footer-develop-div">Powered by <a href="https://github.com/ruibaby/halo" target="_blank" class="footer-develop-a">Halo</a></div>
            <div class="footer-develop-div">Theme - <a href="https://github.com/viosey/hexo-theme-material" target="_blank" class="footer-develop-a">Material</a></div>
        </div>
    </div>
    </#if>
</footer>
