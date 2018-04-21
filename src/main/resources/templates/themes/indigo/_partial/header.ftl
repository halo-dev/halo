<#macro header title="" hdClass="">
<header class="top-header" id="header">
    <div class="flex-row">
        <a href="javascript:;" class="header-icon waves-effect waves-circle waves-light on" id="menu-toggle">
            <i class="icon icon-lg icon-navicon"></i>
        </a>
        <div class="flex-col header-title ellipsis">${title}</div>
        <% if(theme.search){ %>
        <div class="search-wrap" id="search-wrap">
            <a href="javascript:;" class="header-icon waves-effect waves-circle waves-light" id="back">
                <i class="icon icon-lg icon-chevron-left"></i>
            </a>
            <input type="text" id="key" class="search-input" autocomplete="off" placeholder="<%= __('global.search_input_hint') %>">
            <a href="javascript:;" class="header-icon waves-effect waves-circle waves-light" id="search">
                <i class="icon icon-lg icon-search"></i>
            </a>
        </div>
        <% }%>
        <% if(theme.share){ %>
        <a href="javascript:;" class="header-icon waves-effect waves-circle waves-light" id="menuShare">
            <i class="icon icon-lg icon-share-alt"></i>
        </a>
        <% } %>
    </div>
</header>
<header class="content-header ${hdClass}">

    <div class="container fade-scale">
        <h1 class="title">${title}</h1>
        <h5 class="subtitle">
            <% if(is_post()){ %>
            <%- partial('post/head-meta') %>
            <% } else if(is_home()){ %>
            <%- config.subtitle %>
            <% } else if (page.layout === 'page' && page.description) {%>
            <%- page.description %>
            <% } %>
        </h5>
    </div>

    <%- partial('tags-bar', {
    type: hdClass.split('-')[0]
    }) %>

</header>
</#macro>