<header class="header">
    <div class="header-wrapper">
        <!-- Header Copyright -->
        <div class="header-copyright">
            <div class="header-blog">
                ©&nbsp;
                <script type="text/javascript">
                    var fd = new Date();
                    document.write(fd.getFullYear());
                </script>
                &nbsp;${options.blog_title!}
            </div>
            <!--
            I'm glad you use this theme, the development is no so easy, I hope you can keep the copyright.
            It will not impact the appearance and can give developers a lot of support :)

            很高兴您使用该主题，开发不易，希望您可以保留一下版权声明。
            它不会影响美观并可以给开发者很大的支持。 :)
            -->
            <div>
                Powered by <a href="https://github.com/ruibaby/halo.git" target="_blank" class="footer-develop-a">Halo</a>
                <br>
                Theme - <a href="https://github.com/viosey/hexo-theme-material" target="_blank" class="footer-develop-a">Material</a>
            </div>
        </div>

        <!-- Header Title -->
        <span class="header-title header-item">
            <a href="${options.blog_url!}/" title="<%= config.title %>">
                ${options.blog_title!}
            </a>
        </span>

        <p class="header-slogan header-item">
            ${options.theme_material_uiux_slogan!'Hi,nice to meet you'}
        </p>

        <!-- Header Nav -->
        <nav class="header-nav header-item">
            <span class="header-nav-item">
                <a href="${options.blog_url!}/" title="Home">
                    <span>主页</span>
                </a>
            </span>

            <!-- Pages  -->

            <span class="header-nav-item">
                    <a href="${options.blog_url!}/tags" title="标签">
                        <span>标签</span>
                    </a>
                </span>

            <span class="header-nav-item">
                    <a href="${options.blog_url!}/gallery" title="图库">
                        <span>图库</span>
                    </a>
                </span>

            <span class="header-nav-item">
                    <a href="${options.blog_url!}/links" title="友链">
                        <span>友链</span>
                    </a>
                </span>

            <span class="header-nav-item">
                    <a href="${options.blog_url!}/about" title="关于">
                        <span>关于</span>
                    </a>
                </span>
        </nav>

        <!-- Header SNS -->
        <#include "isolate-sns_list.ftl">
    </div>
</header>
