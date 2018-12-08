<meta charset="utf-8">
<title><%= page_title() %></title>
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1" />
<%- meta(page) %>

<% if (has_config('open_graph')) { %>
    <%- open_graph({
        twitter_id: get_config('open_graph.twitter_id'),
        twitter_site: get_config('open_graph.twitter_site'),
        google_plus: get_config('open_graph.google_plus'),
        fb_admins: get_config('open_graph.fb_admins'),
        fb_app_id: get_config('open_graph.fb_app_id')
    }) %>
<% } %>

<% if (has_config('rss')) { %>
<link rel="alternative" href="<%- get_config('rss') %>" title="<%= get_config('title') %>" type="application/atom+xml">
<% } %>

<% if (has_config('favicon')) { %>
<link rel="icon" href="<%- url_for(get_config('favicon')) %>">
<% } %>

<%- css(cdn('bulma', '0.7.2', 'css/bulma.css')) %>
<%- css(iconcdn()) %>
<%- css(fontcdn('Ubuntu:400,600|Source+Code+Pro')) %>
<%- css(cdn('highlight.js', '9.12.0', 'styles/' + get_config('article.highlight') + '.css')) %>
<%- css('css/style') %>

<% if (has_config('plugins')) { %>
    <% for (let plugin in get_config('plugins')) { %>
    <%- partial('plugin/' + plugin, { head: true, plugin: get_config('plugins')[plugin] }) %>
    <% } %>
<% } %>
