const { doc, type, required } = require('../common/utils').descriptors;

module.exports = {
    [type]: 'object',
    [doc]: 'Link icon settings',
    '*': {
        [type]: ['string', 'object'],
        [doc]: 'Path or URL to the menu item, and/or link icon class names',
        icon: {
            [required]: true,
            [type]: 'string',
            [doc]: 'Link icon class names'
        },
        url: {
            [required]: true,
            [type]: 'string',
            [doc]: 'Path or URL to the menu item'
        }
    }
};