/**
 * Find Missing Translations
 * -------------------------
 * This script identifies missing translations in language files by comparing them
 * with the English base file (en.yaml).
 *
 * For each language file, it:
 * 1. Compares it with the English base file
 * 2. Identifies keys that are missing in the target language
 * 3. Creates a "_missing_translations_[lang].yaml" file with those missing keys
 *
 * Usage:
 * node scripts/find_missing_translations.mjs
 *
 * Example output:
 * Generated src/locales/_missing_translations_zh-TW.yaml with 25 missing translations
 *
 * After running this script, you can translate the missing entries in the generated files,
 * then use apply_missing_translations.mjs to merge them into the main language files.
 */

import fs from "fs/promises";
import yaml from "js-yaml";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const translationsDirPath = path.resolve(__dirname, "../src/locales");
const baseFile = `${translationsDirPath}/en.yaml`;

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
      const langCode = path.basename(transFile, ".yaml");

      try {
        const translations = await loadYamlFile(transFile);
        const missing = findMissingTranslations(baseTranslations, translations);

        if (Object.keys(missing).length > 0) {
          const missingFile = `${translationsDirPath}/_missing_translations_${langCode}.yaml`;
          await saveYamlFile(missing, missingFile);
          console.log(
            `Generated ${missingFile} with ${
              Object.keys(missing).length
            } missing translations`
          );
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

function findMissingTranslations(baseDict, compareDict) {
  const missing = {};

  for (const key of Object.keys(baseDict)) {
    if (!Object.prototype.hasOwnProperty.call(compareDict, key)) {
      missing[key] = baseDict[key];
    } else if (
      typeof baseDict[key] === "object" &&
      baseDict[key] !== null &&
      typeof compareDict[key] === "object" &&
      compareDict[key] !== null
    ) {
      const subMissing = findMissingTranslations(
        baseDict[key],
        compareDict[key]
      );
      if (Object.keys(subMissing).length > 0) {
        missing[key] = subMissing;
      }
    }
  }

  return missing;
}

main();
