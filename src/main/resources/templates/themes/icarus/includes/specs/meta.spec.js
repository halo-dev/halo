const { doc, type, defaultValue } = require('../common/utils').descriptors;

module.exports = {
    favicon: {
        [type]: 'string',
        [doc]: 'Path or URL to the website\'s icon',
        [defaultValue]: '/images/favicon.svg',
    },
    rss: {
        [type]: 'string',
        [doc]: 'Path or URL to RSS atom.xml',
        [defaultValue]: null
    },
    logo: {
        [type]: ['string', 'object'],
        [defaultValue]: '/images/logo.svg',
        [doc]: 'Path or URL to the website\'s logo to be shown on the left of the navigation bar or footer',
        text: {
            [type]: 'string',
            [doc]: 'Text to be shown in place of the logo image'
        }
    },
    open_graph: {
        [type]: 'object',
        [doc]: 'Open Graph metadata (https://hexo.io/docs/helpers.html#open-graph)',
        fb_app_id: {
            [type]: 'string',
            [doc]: 'Facebook App ID',
            [defaultValue]: null
        },
        fb_admins: {
            [type]: 'string',
            [doc]: 'Facebook Admin ID',
            [defaultValue]: null
        },
        twitter_id: {
            [type]: 'string',
            [doc]: 'Twitter ID',
            [defaultValue]: null
        },
        twitter_site: {
            [type]: 'string',
            [doc]: 'Twitter site',
            [defaultValue]: null
        },
        google_plus: {
            [type]: 'string',
            [doc]: 'Google+ profile link',
            [defaultValue]: null
        }
    }
};