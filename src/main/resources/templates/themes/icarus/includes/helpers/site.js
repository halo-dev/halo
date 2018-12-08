/**
 * Helper functions related the site properties.
 *
 * @example
*     <%- is_same_link(url_a, url_b) %>
*     <%- post_count() %>
*     <%- category_count() %>
*     <%- tag_count() %>
*     <%- duration() %>
*     <%- word_count(content) %>
 */
const moment = require('moment');

module.exports = function (hexo) {
    hexo.extend.helper.register('is_same_link', function (a, b) {
        function santize(url) {
            let paths = url.replace(/(^\w+:|^)\/\//, '').split('#')[0].split('/').filter(p => p.trim() !== '');
            if (paths.length > 0 && paths[paths.length - 1].trim() === 'index.html') {
                paths = paths.slice(0, paths.length - 1)
            }
            return paths.join('/');
        }
        return santize(this.url_for(a)) == santize(this.url_for(b));
    });

    hexo.extend.helper.register('post_count', function () {
        return this.site.posts.length;
    });

    hexo.extend.helper.register('category_count', function () {
        return this.site.categories.filter(category => category.length).length;
    });

    hexo.extend.helper.register('tag_count', function () {
        return this.site.tags.filter(tag => tag.length).length;
    });

    /**
     * Export moment.duration
     */
    hexo.extend.helper.register('duration', function () {
        return moment.duration.apply(moment, arguments);
    });

    /**
     * Get the word count of a paragraph.
     */
    hexo.extend.helper.register('word_count', function (content) {
        content = content.replace(/<\/?[a-z][^>]*>/gi, '');
        content = content.trim();
        return content ? (content.match(/[\u00ff-\uffff]|[a-zA-Z]+/g) || []).length : 0;
    });
}