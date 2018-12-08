const { doc, type, defaultValue, required, requires } = require('../common/utils').descriptors;

const ChangYanSpec = {
    appid: {
        [type]: 'string',
        [doc]: 'Changyan comment app ID',
        [required]: true,
        [requires]: comment => comment.type === 'changyan'
    },
    conf: {
        [type]: 'string',
        [doc]: 'Changyan comment configuration ID',
        [required]: true,
        [requires]: comment => comment.type === 'changyan'
    }
};

const DisqusSpec = {
    shortname: {
        [type]: 'string',
        [doc]: 'Disqus shortname',
        [required]: true,
        [requires]: comment => comment.type === 'disqus'
    }
};

const GitmentSpec = {
    owner: {
        [type]: 'string',
        [doc]: 'Your GitHub ID',
        [required]: true,
        [requires]: comment => comment.type === 'gitment'
    },
    repo: {
        [type]: 'string',
        [doc]: 'The repo to store comments',
        [required]: true,
        [requires]: comment => comment.type === 'gitment'
    },
    client_id: {
        [type]: 'string',
        [doc]: 'Your client ID',
        [required]: true,
        [requires]: comment => comment.type === 'gitment'
    },
    client_secret: {
        [type]: 'string',
        [doc]: 'Your client secret',
        [required]: true,
        [requires]: comment => comment.type === 'gitment'
    }
};

const IssoSpec = {
    url: {
        [type]: 'string',
        [doc]: 'URL to your Isso comment service',
        [required]: true,
        [requires]: comment => comment.type === 'isso'
    }
};

const LiveReSpec = {
    uid: {
        [type]: 'string',
        [doc]: 'LiveRe comment service UID',
        [required]: true,
        [requires]: comment => comment.type === 'livere'
    }
};

const ValineSpec = {
    app_id: {
        [type]: 'string',
        [doc]: 'LeanCloud APP ID',
        [required]: true,
        [requires]: comment => comment.type === 'valine'
    },
    app_key: {
        [type]: 'string',
        [doc]: 'LeanCloud APP key',
        [required]: true,
        [requires]: comment => comment.type === 'valine'
    },
    notify: {
        [type]: 'boolean',
        [doc]: 'Enable email notification when someone comments',
        [defaultValue]: false,
        [requires]: comment => comment.type === 'valine'
    },
    verify: {
        [type]: 'boolean',
        [doc]: 'Enable verification code service',
        [defaultValue]: false,
        [requires]: comment => comment.type === 'valine'
    },
    placeholder: {
        [type]: 'string',
        [doc]: 'Placeholder text in the comment box',
        [defaultValue]: 'Say something...',
        [requires]: comment => comment.type === 'valine'
    }
};

module.exports = {
    [type]: 'object',
    [doc]: 'Comment plugin settings (http://ppoffice.github.io/hexo-theme-icarus/categories/Configuration/Comment-Plugins)',
    type: {
        [type]: 'string',
        [doc]: 'Name of the comment plugin',
        [defaultValue]: null
    },
    ...ChangYanSpec,
    ...DisqusSpec,
    ...GitmentSpec,
    ...IssoSpec,
    ...LiveReSpec,
    ...ValineSpec
}