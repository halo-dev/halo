/**
 * Helper functions that override Hexo built-in helpers.
 *
 * @example
*     <%- _list_archives() %>
*     <%- _list_categories() %>
*     <%- _list_tags() %>
*     <%- _toc() %>
 */
const cheerio = require('cheerio');

module.exports = function (hexo) {
    hexo.extend.helper.register('_list_archives', function () {
        const $ = cheerio.load(this.list_archives(), { decodeEntities: false });
        const archives = [];
        $('.archive-list-item').each(function () {
            archives.push({
                url: $(this).find('.archive-list-link').attr('href'),
                name: $(this).find('.archive-list-link').text(),
                count: $(this).find('.archive-list-count').text()
            });
        });
        return archives;
    });

    hexo.extend.helper.register('_list_categories', function () {
        const $ = cheerio.load(this.list_categories({ depth: 2 }), { decodeEntities: false });
        function traverse(root) {
            const categories = [];
            root.find('> .category-list-item').each(function () {
                const category = {
                    url: $(this).find('> .category-list-link').attr('href'),
                    name: $(this).find('> .category-list-link').text(),
                    count: $(this).find('> .category-list-count').text()
                };
                if ($(this).find('> .category-list-child').length) {
                    category['children'] = traverse($(this).find('> .category-list-child'));
                }
                categories.push(category);
            });
            return categories;
        }
        return traverse($('.category-list'));
    });

    hexo.extend.helper.register('_list_tags', function () {
        const $ = cheerio.load(this.list_tags(), { decodeEntities: false });
        const tags = [];
        $('.tag-list-item').each(function () {
            tags.push({
                url: $(this).find('.tag-list-link').attr('href'),
                name: $(this).find('.tag-list-link').text(),
                count: $(this).find('.tag-list-count').text()
            });
        });
        return tags;
    });

    /**
     * Export a tree of headings of an article
     * {
     *    "1": {
     *        "id": "How-to-enable-table-of-content-for-a-post",
     *        "index": "1"
     *    },
     *    "2": {
     *        "1": {
     *            "1": {
     *                "id": "Third-level-title",
     *                "index": "2.1.1"
     *            },
     *            "id": "Second-level-title",
     *            "index": "2.1"
     *        },
     *        "2": {
     *            "id": "Another-second-level-title",
     *            "index": "2.2"
     *        },
     *        "id": "First-level-title",
     *        "index": "2"
     *    }
     * }
     */
    hexo.extend.helper.register('_toc', (content) => {
        const $ = cheerio.load(content, { decodeEntities: false });
        const toc = {};
        const levels = [0, 0, 0];
        // Get top 3 headings that are present in the content
        const tags = [1, 2, 3, 4, 5, 6].map(i => 'h' + i).filter(h => $(h).length > 0).slice(0, 3);
        if (tags.length === 0) {
            return toc;
        }
        $(tags.join(',')).each(function () {
            const level = tags.indexOf(this.name);
            const id = $(this).attr('id');
            const text = $(this).text();

            for (let i = 0; i < levels.length; i++) {
                if (i > level) {
                    levels[i] = 0;
                } else if (i < level) {
                    if (levels[i] === 0) {
                        // if headings start with a lower level heading, set the former heading index to 1
                        // e.g. h3, h2, h1, h2, h3 => 1.1.1, 1.2, 2, 2.1, 2.1.1
                        levels[i] = 1;
                    }
                } else {
                    levels[i] += 1;
                }
            }
            let node = toc;
            for (let i of levels.slice(0, level + 1)) {
                if (!node.hasOwnProperty(i)) {
                    node[i] = {};
                }
                node = node[i];
            }
            node.id = id;
            node.text = text;
            node.index = levels.slice(0, level + 1).join('.');
        });
        return toc;
    });
}