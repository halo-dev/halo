const { doc, type, defaultValue } = require('../common/utils').descriptors;

module.exports = {
    [type]: 'object',
    [doc]: 'Other plugin settings',
    gallery: {
        [type]: 'boolean',
        [doc]: 'Enable the lightGallery and Justified Gallery plugins (http://ppoffice.github.io/hexo-theme-icarus/2016/07/08/plugin/Gallery/)',
        [defaultValue]: true
    },
    'outdated-browser': {
        [type]: 'boolean',
        [doc]: 'Enable the Outdated Browser plugin (http://outdatedbrowser.com/)',
        [defaultValue]: true
    },
    animejs: {
        [type]: 'boolean',
        [doc]: 'Enable page animations (http://animejs.com/)',
        [defaultValue]: true
    },
    mathjax: {
        [type]: 'boolean',
        [doc]: 'Enable the MathJax plugin (http://ppoffice.github.io/hexo-theme-icarus/2018/01/01/plugin/MathJax/)',
        [defaultValue]: true
    },
    'back-to-top': {
        [type]: 'boolean',
        [doc]: 'Show the back to top button on mobile devices',
        [defaultValue]: true
    },
    'google-analytics': {
        [type]: ['boolean', 'object'],
        [doc]: 'Google Analytics plugin settings (http://ppoffice.github.io/hexo-theme-icarus/2018/01/01/plugin/Analytics/#Google-Analytics)',
        tracking_id: {
            [type]: 'string',
            [doc]: 'Google Analytics tracking id',
            [defaultValue]: null
        }
    },
    'baidu-analytics': {
        [type]: ['boolean', 'object'],
        [doc]: 'Baidu Analytics plugin settings (http://ppoffice.github.io/hexo-theme-icarus/2018/01/01/plugin/Analytics/#Baidu-Analytics)',
        tracking_id: {
            [type]: 'string',
            [doc]: 'Baidu Analytics tracking id',
            [defaultValue]: null
        }
    },
    hotjar: {
        [type]: ['boolean', 'object'],
        [doc]: 'Hotjar user feedback plugin (http://ppoffice.github.io/hexo-theme-icarus/2018/01/01/plugin/Analytics/#Hotjar)',
        site_id: {
            [type]: ['string', 'number'],
            [doc]: 'Hotjar site id',
            [defaultValue]: null
        }
    },
    progressbar: {
        [type]: 'boolean',
        [doc]: 'Show a loading progress bar at top of the page',
        [defaultValue]: true
    }
};