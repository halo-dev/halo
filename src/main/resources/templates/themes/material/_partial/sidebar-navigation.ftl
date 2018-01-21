<ul class="nav sidebar-nav">
    <!-- User dropdown  -->
    <li class="dropdown">
        <ul id="settings-dropdown" class="dropdown-menu">
            <li>
                <a href="mailto:${user.userEmail?default("")}" target="_blank" title="Email Me">
                    <i class="material-icons sidebar-material-icons sidebar-indent-left1pc-element">email</i>
                    Email Me
                </a>
            </li>
        </ul>
    </li>

    <!-- Homepage -->

    <li id="sidebar-first-li">
        <a href="/">

            <i class="material-icons sidebar-material-icons">home</i>

            主页
        </a>
    </li>
    <li class="divider"></li>
    <!-- Archives  -->
    <li class="dropdown">
        <a href="#" class="ripple-effect dropdown-toggle" data-toggle="dropdown">

            <i class="material-icons sidebar-material-icons">inbox</i>

            归档
            <b class="caret"></b>
        </a>
        <ul class="dropdown-menu">
            <#if archives??>
                <#list archives as archive>
                    <li>
                        <a class="sidebar_archives-link" href="/archives/${archive.year}/${archive.month}/">${archive.month}月 ${archive.year}<span class="sidebar_archives-count">${archive.count}</span></a>
                    </li>
                </#list>
            </#if>
        </ul>
    </li>
    <!-- Categories  -->

    <li class="dropdown">
        <a href="#" class="ripple-effect dropdown-toggle" data-toggle="dropdown">

            <i class="material-icons sidebar-material-icons">chrome_reader_mode</i>

            分类
            <b class="caret"></b>
        </a>
        <ul class="dropdown-menu">
            <#list categories as cate>
                <li>
                    <a class="sidebar_archives-link" href="/categories/${cate.cateUrl}/">${cate.cateName}<span class="sidebar_archives-count">4</span></a>
                </li>
            </#list>
        </ul>
    </li>



    <!-- Pages  -->

    <li>
        <a href="/tags" title="标签">

            <i class="material-icons sidebar-material-icons">merge_type</i>

            标签
        </a>
    </li>


    <li>
        <a href="/gallery" title="图库">

            <i class="material-icons sidebar-material-icons">photo_library</i>

            图库
        </a>
    </li>


    <li>
        <a href="/links" title="友链">

            <i class="material-icons sidebar-material-icons">link</i>

            友链
        </a>
    </li>


    <li>
        <a href="/about" title="关于">

            <i class="material-icons sidebar-material-icons">tag_faces</i>

            关于
        </a>
    </li>



    <!-- Article Number  -->

    <li>
        <a href="/archives">
            文章总数
            <span class="sidebar-badge">16</span>
        </a>
    </li>


</ul>