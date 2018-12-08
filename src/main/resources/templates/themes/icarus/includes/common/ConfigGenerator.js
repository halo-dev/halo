const yaml = require('js-yaml');
const Type = require('js-yaml/lib/js-yaml/type');
const Schema = require('js-yaml/lib/js-yaml/schema');

const { is, descriptors } = require('./utils');
const { doc, type, requires, defaultValue } = descriptors;

const UNDEFINED = Symbol('undefined');
// output null as empty in yaml
const YAML_SCHEMA = new Schema({
    include: [
        require('js-yaml/lib/js-yaml/schema/default_full')
    ],
    implicit: [
        new Type('tag:yaml.org,2002:null', {
            kind: 'scalar',
            resolve(data) {
                if (data === null) {
                    return true;
                }
                const max = data.length;
                return (max === 1 && data === '~') ||
                    (max === 4 && (data === 'null' || data === 'Null' || data === 'NULL'));
            },
            construct: () => null,
            predicate: object => object === null,
            represent: {
                empty: function () { return ''; }
            },
            defaultStyle: 'empty'
        })
    ]
});

function generate(spec, parentConfig = null) {
    if (!is.spec(spec)) {
        return UNDEFINED;
    }
    if (spec.hasOwnProperty(requires) && !spec[requires](parentConfig)) {
        return UNDEFINED;
    }
    if (spec.hasOwnProperty(defaultValue)) {
        return spec[defaultValue];
    }
    const types = is.array(spec[type]) ? spec[type] : [spec[type]];
    if (types.includes('object')) {
        let defaults = UNDEFINED;
        for (let key in spec) {
            if (key === '*') {
                continue;
            }
            const value = generate(spec[key], defaults);
            if (value !== UNDEFINED) {
                if (defaults === UNDEFINED) {
                    defaults = {};
                }
                if (spec[key].hasOwnProperty(doc)) {
                    defaults['#' + key] = spec[key][doc];
                }
                defaults[key] = value;
            }
        }
        return defaults;
    } else if (types.includes('array') && spec.hasOwnProperty('*')) {
        return [generate(spec['*'], {})];
    }
    return UNDEFINED;
}

class ConfigGenerator {
    constructor(spec) {
        this.spec = spec;
    }

    generate() {
        return yaml.safeDump(generate(this.spec), {
            indent: 4,
            lineWidth: 1024,
            schema: YAML_SCHEMA
        }).replace(/^(\s*)\'#.*?\':\s*\'*(.*?)\'*$/mg, '$1# $2'); // make comment lines
    }
}

module.exports = ConfigGenerator;
