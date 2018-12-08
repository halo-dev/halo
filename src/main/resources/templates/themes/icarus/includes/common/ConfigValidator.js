const { is, descriptors } = require('./utils');
const { type, required, requires, format, defaultValue } = descriptors;
const {
    InvalidSpecError,
    MissingRequiredError,
    TypeMismatchError,
    FormatMismatchError,
    VersionMalformedError,
    VersionNotFoundError,
    VersionMismatchError } = require('./utils').errors;

function isRequiresSatisfied(spec, config) {
    try {
        if (!spec.hasOwnProperty(requires) || spec[requires](config) === true) {
            return true;
        }
    } catch (e) { }
    return false;
}

function getConfigType(spec, config) {
    const specTypes = is.array(spec[type]) ? spec[type] : [spec[type]];
    for (let specType of specTypes) {
        if (is[specType](config)) {
            return specType;
        }
    }
    return null;
}

function hasFormat(spec, config) {
    if (!spec.hasOwnProperty(format)) {
        return true;
    }
    return spec[format].test(config);
}

function validate(spec, config, parentConfig, path) {
    if (!is.spec(spec)) {
        throw new InvalidSpecError(spec, path);
    }
    if (!isRequiresSatisfied(spec, parentConfig)) {
        return;
    }
    if (is.undefined(config) || is.null(config)) {
        if (spec[required] === true) {
            throw new MissingRequiredError(spec, path);
        }
        return;
    }
    const type = getConfigType(spec, config);
    if (type === null) {
        throw new TypeMismatchError(spec, path, config);
    }
    if (type === 'string') {
        if (!hasFormat(spec, config)) {
            throw new FormatMismatchError(spec, path, config);
        }
    } else if (type === 'array' && spec.hasOwnProperty('*')) {
        config.forEach((child, i) => validate(spec['*'], child, config, path.concat(`[${i}]`)));
    } else if (type === 'object') {
        for (let key in spec) {
            if (key === '*') {
                Object.keys(config).forEach(k => validate(spec['*'], config[k], config, path.concat(k)));
            } else {
                validate(spec[key], config[key], config, path.concat(key));
            }
        }
    }
}

function formatVersion(ver) {
    const m = /^(\d)+\.(\d)+\.(\d)+(?:-([0-9A-Za-z-]+))*$/.exec(ver);
    if (m === null) {
        throw new VersionMalformedError(ver);
    }
    return {
        major: m[1],
        minor: m[2],
        patch: m[3],
        identifier: m.length > 4 ? m[4] : null
    };
}

function compareVersion(ver1, ver2) {
    for (let section of ['major', 'minor', 'patch']) {
        if (ver1[section] !== ver2[section]) {
            return Math.sign(ver1[section] - ver2[section]);
        }
    }
    const id1 = ver1.hasOwnProperty('identifier') ? ver1.identifier : null;
    const id2 = ver2.hasOwnProperty('identifier') ? ver2.identifier : null;
    if (id1 === id2) {
        return 0;
    }
    if (id1 === null) {
        return 1;
    }
    if (id2 === null) {
        return -1;
    }
    return id1.localeCompare(id2);
}

function isBreakingChange(base, ver) {
    return base.major !== ver.major;
}


function checkVersion(spec, config) {
    if (!config.hasOwnProperty('version')) {
        throw new VersionNotFoundError();
    }
    const configVersion = formatVersion(config.version);
    const specVersion = formatVersion(spec.version[defaultValue]);
    if (isBreakingChange(specVersion, configVersion)) {
        throw new VersionMismatchError(spec.version[defaultValue], config.version, compareVersion(specVersion, configVersion) > 0);
    }
}

class ConfigValidator {
    constructor(spec) {
        this.spec = spec;
    }

    validate(config) {
        checkVersion(this.spec, config);
        validate(this.spec, config, null, []);
    }
}

module.exports = ConfigValidator;