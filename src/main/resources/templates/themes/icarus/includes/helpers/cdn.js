/**
 * CDN static file resolvers.
 *
 * @example
 *     <%- cdn(package, version, filename) %>
 *     <%- fontcdn(fontName) %>
 *     <%- iconcdn() %>
 */
const cdn_providers = {
    cdnjs: 'https://cdnjs.cloudflare.com/ajax/libs/${ package }/${ version }/${ filename }',
    jsdelivr: 'https://cdn.jsdelivr.net/npm/${ package }@${ version }/${ filename }',
    unpkg: 'https://unpkg.com/${ package }@${ version }/${ filename }'
};

const font_providers = {
    google: 'https://fonts.googleapis.com/${ type }?family=${ fontname }'
};

const icon_providers = {
    fontawesome: 'https://use.fontawesome.com/releases/v5.4.1/css/all.css'
};

module.exports = function (hexo) {
    hexo.extend.helper.register('cdn', function (_package, version, filename) {
        let provider = hexo.extend.helper.get('get_config').bind(this)('providers.cdn');
        // cdn.js does not follow a GitHub npm style like jsdeliver and unpkg do. Patch it!
        if (provider === 'cdnjs' || provider.startsWith('[cdnjs]')) {
            if (provider.startsWith('[cdnjs]')) {
                provider = provider.substr(7);
            }
            if (filename.startsWith('dist/')) {
                filename = filename.substr(5);
            }
            if (_package === 'moment') {
                _package = 'moment.js';
                filename = filename.startsWith('min/') ? filename.substr(4) : filename;
            }
            if (_package === 'outdatedbrowser') {
                _package = 'outdated-browser';
                filename = filename.startsWith('outdatedbrowser/') ? filename.substr(16) : filename;
            }
            if (_package === 'highlight.js') {
                filename = filename.endsWith('.css') && filename.indexOf('.min.') === -1 ?
                    filename.substr(0, filename.length - 4) + '.min.css' : filename;
            }
            if (_package === 'mathjax') {
                filename = filename.startsWith('unpacked/') ? filename.substr(9) : filename;
            }
            if (_package === 'pace-js') {
                _package = 'pace';
            }
        }
        if (provider !== null && cdn_providers.hasOwnProperty(provider)) {
            provider = cdn_providers[provider];
        }
        return provider.replace(/\${\s*package\s*}/gi, _package)
            .replace(/\${\s*version\s*}/gi, version)
            .replace(/\${\s*filename\s*}/gi, filename);
    });

    hexo.extend.helper.register('fontcdn', function (fontName, type = 'css') {
        let provider = hexo.extend.helper.get('get_config').bind(this)('providers.fontcdn');
        if (provider !== null && font_providers.hasOwnProperty(provider)) {
            provider = font_providers[provider];
        }
        return provider.replace(/\${\s*fontname\s*}/gi, fontName)
            .replace(/\${\s*type\s*}/gi, type);
    });

    hexo.extend.helper.register('iconcdn', function (provider = null) {
        if (provider !== null && icon_providers.hasOwnProperty(provider)) {
            provider = icon_providers[provider];
        } else {
            provider = hexo.extend.helper.get('get_config').bind(this)('providers.iconcdn');
            if (provider !== null && icon_providers.hasOwnProperty(provider)) {
                provider = icon_providers[provider];
            }
        }
        return provider;
    });
}