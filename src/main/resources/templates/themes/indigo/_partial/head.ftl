<head>
    <%- partial('plugins/google-analytics') %>
    <%- partial('plugins/tajs') %>
    <%- partial('plugins/baidu') %>
    <meta charset="utf-8">
    <% if(theme.google_site_verification){ %>
    <meta name="google-site-verification" content="<%- theme.google_site_verification%>">
    <% }%>
    <% if(theme.sogou_site_verification){ %>
    <meta name="sogou_site_verification" content="<%- theme.sogou_site_verification%>">
    <% }%>
    <% if(theme.canonical){ %>
    <link rel="canonical" href="<%- (theme.canonical + url_for(page.path)).replace(/index\.html$/, '') %>">
    <% }%>
    <%
        var title = page.title;

        if (is_archive()){
            title = theme.archives_title || 'Archives';

            if (is_month()){
            title += ': ' + page.year + '/' + page.month;
            } else if (is_year()){
            title += ': ' + page.year;
            }
        } else if (is_category()){
            title = (theme.categories_title || 'Categories') + ': ' + page.category;
        } else if (is_tag()){
            title = (theme.tags_title || 'Tags') + ': ' + page.tag;
        }
    %>
    <title><% if (title){ %><%= title %> | <% } %><%= config.title %><% if (config.subtitle){ %> | <%= config.subtitle %><% } %></title>
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <% if(theme.color){ %>
    <meta name="theme-color" content="<%=theme.color%>">
    <% } %>
    <%
        var keyWords = config.keywords;
        if(page.tags){
        keyWords = [];

        _.isArray(page.tags) ? ( keyWords = page.tags )
            : page.tags.each(function(k){
                keyWords.push(k.name);
            });
    }
    %>
    <meta name="keywords" content="<%= keyWords %>">
    <%- open_graph({twitter_id: theme.twitter, google_plus: theme.google_plus, fb_admins: theme.fb_admins, fb_app_id: theme.fb_app_id}) %>
    <% if ((config.feed) && (config.feed.path.length)) { %>
        <link rel="alternate" type="application/atom+xml" title="<%= config.title %>" href="<%- url_for(config.feed.path) %>">
    <% } %>
    <%- favicon_tag(theme.favicon) %>
    <link rel="stylesheet" href="<%- url_for(theme_css('/css/style', cache)) %>">
    <script>window.lazyScripts=[]</script>

    <!-- custom head -->
    <% if (site.data.head) { %>
        <% for (var i in site.data.head) { %>
            <%- site.data.head[i] %>
        <% } %>
    <% } %>

</head>
