const { doc, type, defaultValue, required, requires } = require('../common/utils').descriptors;

module.exports = {
    [type]: 'object',
    [doc]: 'Search plugin settings (http://ppoffice.github.io/hexo-theme-icarus/categories/Configuration/Search-Plugins)',
    type: {
        [type]: 'string',
        [doc]: 'Name of the search plugin',
        [defaultValue]: 'insight'
    },
    cx: {
        [type]: 'string',
        [doc]: 'Google CSE cx value',
        [required]: true,
        [requires]: search => search.type === 'google-cse'
    }
};