<footer class="footer">
    <div class="container">
        <div class="level">
            <div class="level-start has-text-centered-mobile">
                <a class="footer-logo is-block has-mb-6" href="<%- url_for('/') %>">
                <% if (has_config('logo.text') && get_config('logo.text')) { %>
                    <%= get_config('logo.text') %>
                <% } else { %>
                    <img src="<%- url_for(get_config('logo')) %>" alt="" height="28">
                <% } %>
                </a>
                <p class="is-size-7">
                &copy; <%= date(new Date(), 'YYYY') %> <%= get_config('author') || get_config('title') %>&nbsp;
                Powered by <a href="http://hexo.io/" target="_blank">Hexo</a> & <a
                        href="http://github.com/ppoffice/hexo-theme-icarus">Icarus</a>
                </p>
            </div>
            <div class="level-end">
            <% if (has_config('footer.links')) { %>
                <div class="field has-addons is-flex-center-mobile has-mt-5-mobile is-flex-wrap is-flex-middle">
                <% let links = get_config('footer.links'); %>
                <% for (let name in links) {
                        let link = links[name]; %>
                <p class="control">
                    <a class="button is-white <%= typeof(link) !== 'string' ? 'is-large' : '' %>" target="_blank" title="<%= name %>" href="<%= url_for(typeof(link) === 'string' ? link : link.url) %>">
                        <% if (typeof(link) === 'string') { %>
                        <%= name %>
                        <% } else { %>
                        <i class="<%= link.icon %>"></i>
                        <% } %>
                    </a>
                </p>
                <% } %>
                </div>
            <% } %>
            </div>
        </div>
    </div>
</footer>