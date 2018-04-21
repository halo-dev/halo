<#include "_partial/header.ftl">
<@header title="${options.blog_title}" hdClass="index-header"></@header>
<div class="container body-wrap">

    <ul class="post-list">
        <% page.posts.each(function(post){ %>
        <li class="post-list-item fade">
            <%- partial('_partial/index-item', {
            post: post,
            index: true
            }) %>
        </li>
        <% }) %>
    </ul>

    <%- partial('_partial/paginator') %>

</div>