const cheerio = require('cheerio');

module.exports = function (hexo) {
    function patchCodeHighlight(content) {
        const $ = cheerio.load(content, { decodeEntities: false });
        $('figure.highlight').addClass('hljs');
        $('figure.highlight .code .line span').each(function () {
            const classes = $(this).attr('class').split(' ');
            if (classes.length === 1) {
                $(this).addClass('hljs-' + classes[0]);
                $(this).removeClass(classes[0]);
            }
        });
        return $.html();
    }
    
    /**
     * Add .hljs class name to the code blocks and code elements
     */
    hexo.extend.filter.register('after_post_render', function (data) {
        data.content = data.content ? patchCodeHighlight(data.content) : data.content;
        data.excerpt = data.excerpt ? patchCodeHighlight(data.excerpt) : data.excerpt;
        return data;
    });
}