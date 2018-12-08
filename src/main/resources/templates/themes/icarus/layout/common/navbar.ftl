<nav class="navbar navbar-main">
    <div class="container">
        <div class="navbar-brand is-flex-center">
            <a class="navbar-item navbar-logo" href="<%- url_for('/') %>">
            <% if (has_config('logo.text') && get_config('logo.text')) { %>
                <%= get_config('logo.text') %>
            <% } else { %>
                <img src="<%- url_for(get_config('logo')) %>" alt="" height="28">
            <% } %>
            </a>
        </div>
        <div class="navbar-menu">
            <% if (has_config('navbar.menu')) { %>
            <div class="navbar-start">
                <% for (let i in get_config('navbar.menu')) { let menu = get_config('navbar.menu')[i]; %>
                <a class="navbar-item<% if (typeof(page.path) !== 'undefined' && is_same_link(menu, page.path)) { %> is-active<% } %>"
                href="<%- url_for(menu) %>"><%= i %></a>
                <% } %>
            </div>
            <% } %>
            <div class="navbar-end">
                <% if (has_config('navbar.links')) { %>
                    <% let links = get_config('navbar.links'); %>
                    <% for (let name in links) {
                            let link = links[name]; %>
                    <a class="navbar-item" target="_blank" title="<%= name %>" href="<%= url_for(typeof(link) === 'string' ? link : link.url) %>">
                        <% if (typeof(link) === 'string') { %>
                        <%= name %>
                        <% } else { %>
                        <i class="<%= link.icon %>"></i>
                        <% } %>
                    </a>
                    <% } %>
                <% } %>
                <% if (has_config('search.type')) { %>
                <a class="navbar-item search" title="<%= __('search.search') %>" href="javascript:;">
                    <i class="fas fa-search"></i>
                </a>
                <% } %>
            </div>
        </div>
    </div>
</nav>