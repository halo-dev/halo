<ul class="nav sidebar-nav">
    <!-- User dropdown  -->
    <li class="dropdown">
        <ul id="settings-dropdown" class="dropdown-menu">
            <li>
                <a href="mailto:${user.userEmail!}" target="_blank" title="Email Me">
                    <i class="material-icons sidebar-material-icons sidebar-indent-left1pc-element">email</i>
                    Email Me
                </a>
            </li>
        </ul>
    </li>
    <!-- Homepage -->
    <li id="sidebar-first-li">
        <a href="${options.blog_url!}/">
            <i class="material-icons sidebar-material-icons">home</i>
            主页
        </a>
    </li>
    <li class="divider"></li>

    <#if (options.theme_material_other_sidebar_archives!'true') == 'true'>
    <!-- Archives  -->
    <li class="dropdown">
        <a href="#" class="ripple-effect dropdown-toggle" data-toggle="dropdown">
            <i class="material-icons sidebar-material-icons">inbox</i>
            归档
            <b class="caret"></b>
        </a>
        <ul class="dropdown-menu">
            <@articleTag method="archives">
                <#if archives??>
                    <#list archives?sort_by("year")?reverse as archive>
                        <li>
                            <a class="sidebar_archives-link" href="${options.blog_url!}/archives/${archive.year}/${archive.month}/">${archive.month}月 ${archive.year}<span class="sidebar_archives-count">${archive.count}</span></a>
                        </li>
                    </#list>
                </#if>
            </@articleTag>
        </ul>
    </li>
    </#if>

    <#if (options.theme_material_other_sidebar_cates!'true') == 'true'>
    <!-- Categories  -->
    <li class="dropdown">
        <a href="#" class="ripple-effect dropdown-toggle" data-toggle="dropdown">
            <i class="material-icons sidebar-material-icons">chrome_reader_mode</i>
            分类
            <b class="caret"></b>
        </a>
        <ul class="dropdown-menu">
            <@commonTag method="categories">
                <#list categories as cate>
                    <li>
                        <a class="sidebar_archives-link" href="${options.blog_url!}/categories/${cate.cateUrl}/">${cate.cateName}<span class="sidebar_archives-count">${cate.posts?size}</span></a>
                    </li>
                </#list>
            </@commonTag>
        </ul>
    </li>
    </#if>

    <!-- Pages  -->
    <@commonTag method="menus">
        <#list menus?sort_by("menuSort") as menu>
            <li>
                <a href="${menu.menuUrl}" title="${menu.menuName}">
                    <#if menu.menuIcon!="">
                        <i class="material-icons sidebar-material-icons">${menu.menuIcon}</i>
                    </#if>
                    ${menu.menuName}
                </a>
            </li>
        </#list>
    </@commonTag>

    <#if (options.theme_material_other_sidebar_postcount!'true') == 'true'>
    <!-- Article Number  -->
    <li>
        <a href="${options.blog_url!}/archives">
            文章总数
            <@articleTag method="postsCount">
                <span class="sidebar-badge">${postsCount}</span>
            </@articleTag>
        </a>
    </li>
    </#if>
</ul>
