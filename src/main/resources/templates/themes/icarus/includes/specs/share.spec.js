const { doc, type, defaultValue, required, requires } = require('../common/utils').descriptors;

module.exports = {
    [type]: 'object',
    [doc]: 'Share plugin settings (http://ppoffice.github.io/hexo-theme-icarus/categories/Configuration/Share-Plugins)',
    type: {
        [type]: 'string',
        [doc]: 'Share plugin name',
        [defaultValue]: null
    },
    install_url: {
        [type]: 'string',
        [doc]: 'URL to the share plugin script provided by share plugin service provider',
        [required]: true,
        [requires]: share => share.type === 'sharethis' || share.type === 'addthis'
    }
}