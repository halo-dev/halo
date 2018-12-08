const { doc, type, defaultValue } = require('../common/utils').descriptors;

module.exports = {
    [type]: 'object',
    [doc]: 'Article display settings',
    highlight: {
        [type]: 'string',
        [doc]: 'Code highlight theme (https://github.com/highlightjs/highlight.js/tree/master/src/styles)',
        [defaultValue]: 'atom-one-light'
    },
    thumbnail: {
        [type]: 'boolean',
        [doc]: 'Whether to show article thumbnail images',
        [defaultValue]: true
    },
    readtime: {
        [type]: 'boolean',
        [doc]: 'Whether to show estimate article reading time',
        [defaultValue]: true
    }
};