const fs = require('fs');
const util = require('util');
const path = require('path');
const logger = require('hexo-log')();
const yaml = require('js-yaml');

const { errors } = require('../common/utils');
const rootSpec = require('../specs/config.spec');
const ConfigValidator = require('../common/ConfigValidator');
const ConfigGenerator = require('../common/ConfigGenerator');

const CONFIG_PATH = path.join(__dirname, '../..', '_config.yml');

logger.info('Validating the configuration file');

if (!fs.existsSync(CONFIG_PATH)) {
    const relativePath = path.relative(process.cwd(), CONFIG_PATH);
    logger.warn(`${relativePath} is not found. We are creating one for you...`);
    fs.writeFileSync(CONFIG_PATH, new ConfigGenerator(rootSpec).generate());
    logger.info(`${relativePath} is created. Please restart Hexo to apply changes.`);
    process.exit(0);
}

const validator = new ConfigValidator(rootSpec);
const config = yaml.safeLoad(fs.readFileSync(CONFIG_PATH));
try {
    validator.validate(config);
} catch (e) {
    if (e instanceof errors.ConfigError) {
        logger.error(e.message);
        if (e.hasOwnProperty('spec')) {
            logger.error('The specification of this configuration is:');
            logger.error(util.inspect(e.spec));
        }
        if (e.hasOwnProperty('config')) {
            logger.error('Configuration value is:');
            logger.error(util.inspect(e.config));
        }
    } else if (e instanceof errors.VersionError) {
        logger.error(e.message);
        logger.warn(`To let us create a fresh configuration file for you, please rename or delete the following file:`);
        logger.warn(CONFIG_PATH);
    } else {
        throw e;
    }
}
