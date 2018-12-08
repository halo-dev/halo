const { doc, type, defaultValue } = require('../common/utils').descriptors;

module.exports = {
    [type]: 'object',
    [doc]: 'Footer section link settings',
    links: {
        ...require('./icon_link.spec'),
        [doc]: 'Links to be shown on the right of the footer section',
        [defaultValue]: {
            'Creative Commons': {
                icon: 'fab fa-creative-commons',
                url: 'https://creativecommons.org/'
            },
            'Attribution 4.0 International': {
                icon: 'fab fa-creative-commons-by',
                url: 'https://creativecommons.org/licenses/by/4.0/'
            },
            'Download on GitHub': {
                icon: 'fab fa-github',
                url: 'http://github.com/ppoffice/hexo-theme-icarus'
            }
        }
    }
};