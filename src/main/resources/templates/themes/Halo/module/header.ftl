<header class="main-header " data-aos="fade-down" data-aos-duration="600">
    <nav class="navbar navbar-default hidden-xs">
        <div class="container">
            <div class="navbar-logo">
                <a href="/">
                    <h1><span style="color: #eb437d!important;">R</span>YAN0UP</h1>
                </a>
            </div>
            <div class="navbar-menu pull-right hidden-xs">
                <ul>
                    <@commonTag method="menus">
                        <#list menus?sort_by('menuSort') as menu>
                            <li>
                                <a href="${menu.menuUrl}">${menu.menuName}</a>
                            </li>
                        </#list>
                    </@commonTag>
                </ul>
            </div>
        </div>
    </nav>
    <nav class="navbar-moblie visible-xs">
        <div class="container navbar-moblie-main">
            <div class="navbar-moblie-logo">
                <a href="/">
                    <h1><span style="color: #eb437d!important;">R</span>YAN0UP</h1>
                </a>
            </div>
            <div class="navbar-moblie-btn close" data-aos="fade-down">
                <div class="btn-nav">
                    <div class="line lineTop"></div>
                    <div class="line lineCenter"></div>
                    <div class="line lineBottom"></div>
                </div>
            </div>
        </div>
    </nav>
</header>
<div class="navbar-panel animated fadeInRight hide" style="animation-duration: .5s">
    <div class="navbar-moblie-menu">
        <ul>
            <li class="animated fadeInRight" style="animation-delay: 0s">
                <div>
                    <a href="/"><h1 style="font-size: 28px;">HOME</h1></a>
                </div>
            </li>
            <li class="animated fadeInRight" style="animation-delay: 0.1s">
                <div>
                    <a href="/links"><h1 style="font-size: 28px;">LINKS</h1></a>
                </div>
            </li>
            <li class="animated fadeInRight" style="animation-delay: 0.2s">
                <div>
                    <a href="/archives"><h1 style="font-size: 28px;">ARCHIVES</h1></a>
                </div>
            </li>
            <li class="animated fadeInRight" style="animation-delay: 0.3s">
                <div>
                    <a href="#"><h1 style="font-size: 28px;">ABOUT</h1></a>
                </div>
            </li>
        </ul>
    </div>
</div>