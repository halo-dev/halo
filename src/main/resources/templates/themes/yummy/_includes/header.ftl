<#macro header url>
<header class="site-header">
    <div class="container">
        <a id="site-header-brand" href="/" title="${options.blog_title?default('Halo')}">
            <span class="octicon octicon-mark-github"></span> ${options.blog_title?default('Halo')}
        </a>
        <nav class="site-header-nav" role="navigation">
            <@commonTag method="menus">
                <#list menus as menu>
                    <a href="${menu.menuUrl}"
                       class="<#if url == menu.menuUrl> selected </#if> site-header-nav-item hvr-underline-from-center"
                       target="${menu.menuTarget?default('_self')}"
                       title="${menu.menuName}">
                        ${menu.menuName}
                    </a>
                </#list>
            </@commonTag>
        </nav>
    </div>
</header>
</#macro>
