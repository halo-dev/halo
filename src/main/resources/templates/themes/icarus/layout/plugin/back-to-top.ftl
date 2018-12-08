<% if (plugin !== false) { %>
<% if (!head) { %>
<a id="back-to-top" title="<%= __('plugin.backtotop') %>" href="javascript:;">
    <i class="fas fa-chevron-up"></i>
</a>
<style>
#back-to-top {
    position: fixed;
    padding: 8px 0;
    transition: 0.4s ease opacity, 0.4s ease width, 0.4s ease transform, 0.4s ease border-radius;
    opacity: 0;
    line-height: 24px;
    outline: none;
    transform: translateY(120px);
}
#back-to-top.fade-in {
    opacity: 1;
}
#back-to-top.rise-up {
    transform: translateY(0);
}
</style>
<%- js('js/back-to-top') %>
<% } %>
<% } %>