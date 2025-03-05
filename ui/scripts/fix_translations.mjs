/**
 * Fix Translations
 * -------------------------
 * This script removes translation keys that exist in language files but are not
 * present in the English base file (en.yaml).
 *
 * For each language file, it:
 * 1. Compares it with the English base file
 * 2. Identifies keys that exist in the language file but not in the English file
 * 3. Removes these extra keys from the language file
 *
 * Usage:
 * node scripts/fix_translations.mjs
 *
 * Example output:
 * Extra key found: common.outdatedKey
 * Removed 5 extra keys from src/locales/zh-TW.yaml
 *
 * This script helps maintain consistency across language files by ensuring they
 * only contain keys that are present in the English base file.
 */

import fs from "fs/promises";
import yaml from "js-yaml";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const translationsDirPath = path.resolve(__dirname, "../src/locales");
const baseFile = path.join(translationsDirPath, "en.yaml");

async function main() {
  try {
    const baseTranslations = await loadYamlFile(baseFile);

    const dirEntries = await fs.readdir(translationsDirPath, {
      withFileTypes: true,
    });

    const translationFiles = dirEntries
      .filter(
        (entry) =>
          entry.isFile() &&
          entry.name.endsWith(".yaml") &&
          entry.name !== "en.yaml" &&
          !entry.name.includes("_missing_translations_")
      )
      .map((entry) => path.join(translationsDirPath, entry.name));

    for (const transFile of translationFiles) {
      try {
        const translations = await loadYamlFile(transFile);

        const extraKeysCount = removeExtraTranslations(
          translations,
          baseTranslations
        );

        if (extraKeysCount > 0) {
          await saveYamlFile(translations, transFile);
          console.log(`Removed ${extraKeysCount} extra keys from ${transFile}`);
        } else {
          console.log(`No extra keys found in ${transFile}`);
        }
      } catch (e) {
        console.log(`Error processing ${transFile}: ${e}`);
      }
    }
  } catch (e) {
    console.log(`Error: ${e}`);
  }
}

async function loadYamlFile(filePath) {
  const content = await fs.readFile(filePath, "utf8");
  return yaml.load(content) || {};
}

async function saveYamlFile(data, filePath) {
  const yamlContent = yaml.dump(data, {
    indent: 2,
    lineWidth: -1,
  });
  await fs.writeFile(filePath, yamlContent, "utf8");
}

function removeExtraTranslations(translations, baseTranslations) {
  let extraKeysCount = 0;

  function cleanObject(obj, baseObj, path = "") {
    const keysToDelete = [];

    for (const key of Object.keys(obj)) {
      if (!Object.prototype.hasOwnProperty.call(baseObj, key)) {
        keysToDelete.push(key);
        extraKeysCount++;
        console.log(`Extra key found: ${path}${key}`);
      } else if (
        typeof obj[key] === "object" &&
        obj[key] !== null &&
        typeof baseObj[key] === "object" &&
        baseObj[key] !== null
      ) {
        cleanObject(obj[key], baseObj[key], `${path}${key}.`);
      }
    }

    for (const key of keysToDelete) {
      delete obj[key];
    }
  }

  cleanObject(translations, baseTranslations);
  return extraKeysCount;
}

main();
