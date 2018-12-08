const util = require('hexo-util');

/**
 * Insight search content.json generator.
 */
module.exports = function (hexo) {
    hexo.extend.generator.register('insight', function (locals) {
        const url_for = hexo.extend.helper.get('url_for').bind(this);
        function minify(str) {
            return util.stripHTML(str).trim().replace(/\n/g, ' ').replace(/\s+/g, ' ')
                .replace(/&#x([\da-fA-F]+);/g, function (match, hex) {
                    return String.fromCharCode(parseInt(hex, 16));
                })
                .replace(/&#([\d]+);/g, function (match, dec) {
                    return String.fromCharCode(dec);
                });
        }
        function postMapper(post) {
            return {
                title: post.title,
                text: minify(post.content),
                link: url_for(post.path)
            }
        }
        function tagMapper(tag) {
            return {
                name: tag.name,
                slug: tag.slug,
                link: url_for(tag.path)
            }
        }
        const site = {
            pages: locals.pages.map(postMapper),
            posts: locals.posts.map(postMapper),
            tags: locals.tags.map(tagMapper),
            categories: locals.categories.map(tagMapper)
        };
        return {
            path: '/content.json',
            data: JSON.stringify(site)
        };
    });
}