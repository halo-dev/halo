<% function link_url(i) {
    return url_for(i === 1 ? page.base : page.base + get_config('pagination_dir') + '/' + i + '/');
}

function pagination(c, m) {
    var current = c,
            last = m,
            delta = 1,
            left = current - delta,
            right = current + delta + 1,
            range = [],
            elements = [],
            l;

    for (let i = 1; i <= last; i++) {
        if (i == 1 || i == last || (i >= left && i < right)) {
            range.push(i);
        }
    }

    for (let i of range) {
        if (l) {
            if (i - l === 2) {
                elements.push(`<li><a class="pagination-link has-text-black-ter" href="${ link_url(l + 1) }">${ l + 1 }</a></li>`);
            } else if (i - l !== 1) {
                elements.push(`<li><span class="pagination-ellipsis has-text-black-ter">&hellip;</span></li>`);
            }
        }
        elements.push(`<li><a class="pagination-link${ c === i ? ' is-current' : ' has-text-black-ter'}" href="${ link_url(i) }">${ i }</a></li>`);
        l = i;
    }
    return elements;
} %>
<div class="card card-transparent">
    <nav class="pagination is-centered" role="navigation" aria-label="pagination">
        <div class="pagination-previous<%= page.current > 1 ? '' : ' is-invisible is-hidden-mobile' %>">
            <a class="is-flex-grow has-text-black-ter" href="<%= link_url(page.current - 1) %>"><%= __('common.prev') %></a>
        </div>
        <div class="pagination-next<%= page.current < page.total ? '' : ' is-invisible is-hidden-mobile' %>">
            <a class="is-flex-grow has-text-black-ter" href="<%= link_url(page.current + 1) %>"><%= __('common.next') %></a>
        </div>
        <ul class="pagination-list is-hidden-mobile">
            <% pagination(page.current, page.total).forEach(element => { %>
            <%- element %>
            <% }) %>
        </ul>
    </nav>
</div>