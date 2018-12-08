const doc = Symbol('@doc');
const type = Symbol('@type');
const format = Symbol('@format');
const required = Symbol('@required');
const requires = Symbol('@requires');
const defaultValue = Symbol('@default');

const descriptors = {
    doc,
    type,
    format,
    requires,
    required,
    defaultValue
};

const is = (() => ({
    number(value) {
        return typeof (value) === 'number';
    },
    string(value) {
        return typeof (value) === 'string';
    },
    array(value) {
        return Array.isArray(value);
    },
    boolean(value) {
        return typeof (value) === 'boolean';
    },
    object(value) {
        return typeof (value) === 'object' && value.constructor == Object;
    },
    function(value) {
        return typeof (value) === 'function';
    },
    regexp(value) {
        return value instanceof RegExp;
    },
    undefined(value) {
        return typeof (value) === 'undefined';
    },
    null(value) {
        return value === null;
    },
    spec(value) {
        if (!value.hasOwnProperty(type)) {
            return false;
        }
        if (!is.string(value[type]) && !is.array(value[type])) {
            return false;
        }
        if (value.hasOwnProperty(doc) && !is.string(value[doc])) {
            return false;
        }
        if (value.hasOwnProperty(required) && !is.boolean(value[required])) {
            return false;
        }
        if (value.hasOwnProperty(requires) && !is.function(value[requires])) {
            return false;
        }
        if (value.hasOwnProperty(format) && !is.regexp(value[format])) {
            return false;
        }
        return true;
    }
}))();

class ConfigError extends Error {
    constructor(spec, path) {
        super();
        this.spec = spec;
        this.path = path;
    }
}

class InvalidSpecError extends ConfigError {
    constructor(spec, path) {
        super(spec, path);
        this.message = `The specification '${path.join('.')}' is invalid.`;
    }
}

class MissingRequiredError extends ConfigError {
    constructor(spec, path) {
        super(spec, path);
        this.message = `Configuration file do not have the required '${path.join('.')}' field.`;
    }
}

class TypeMismatchError extends ConfigError {
    constructor(spec, path, config) {
        super(spec, path);
        this.config = config;
        this.message = `Configuration '${path.join('.')}' is not one of the '${spec[type]}' type.`;
    }
}

class FormatMismatchError extends ConfigError {
    constructor(spec, path, config) {
        super(spec, path);
        this.config = config;
        this.message = `Configuration '${path.join('.')}' do not match the format '${spec[format]}'.`;
    }
}

class VersionError extends Error {
}

class VersionNotFoundError extends VersionError {
    constructor() {
        super(`Version number is not found in the configuration file.`);
    }
}

class VersionMalformedError extends VersionError {
    constructor(version) {
        super(`Version number ${version} is malformed.`);
        this.version = version;
    }
}

class VersionMismatchError extends VersionError {
    constructor(specVersion, configVersion, isConfigVersionSmaller) {
        super();
        this.specVersion = specVersion;
        this.configVersion = configVersion;
        if (isConfigVersionSmaller) {
            this.message = `The configuration version ${configVersion} is far behind the specification version ${specVersion}.`;
        } else {
            this.message = `The configuration version ${configVersion} is way ahead of the specification version ${specVersion}.`;
        }
    }
}

const errors = {
    ConfigError,
    InvalidSpecError,
    MissingRequiredError,
    TypeMismatchError,
    FormatMismatchError,
    VersionError,
    VersionMalformedError,
    VersionNotFoundError,
    VersionMismatchError
}

module.exports = {
    is,
    descriptors,
    errors
};