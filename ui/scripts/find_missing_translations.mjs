/**
 * Find Missing Translations
 * -------------------------
 * This script identifies missing translations in language files by comparing them
 * with the English base file (en.json).
 *
 * For each language file, it:
 * 1. Compares it with the English base file
 * 2. Identifies keys that are missing in the target language
 * 3. Creates a "_missing_translations_[lang].json" file with those missing keys
 *
 * Usage:
 * node scripts/find_missing_translations.mjs
 *
 * Example output:
 * Generated src/locales/_missing_translations_zh-TW.json with 25 missing translations
 *
 * After running this script, you can translate the missing entries in the generated files,
 * then use apply_missing_translations.mjs to merge them into the main language files.
 */

import fs from "fs/promises";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const translationsDirPath = path.resolve(__dirname, "../src/locales");
const baseFile = `${translationsDirPath}/en.json`;

async function main() {
  try {
    const baseTranslations = await loadJsonFile(baseFile);

    const dirEntries = await fs.readdir(translationsDirPath, {
      withFileTypes: true,
    });

    const translationFiles = dirEntries
      .filter(
        (entry) =>
          entry.isFile() &&
          entry.name.endsWith(".json") &&
          entry.name !== "en.json" &&
          !entry.name.includes("_missing_translations_")
      )
      .map((entry) => path.join(translationsDirPath, entry.name));

    for (const transFile of translationFiles) {
      const langCode = path.basename(transFile, ".json");

      try {
        const translations = await loadJsonFile(transFile);
        const missing = findMissingTranslations(baseTranslations, translations);

        if (Object.keys(missing).length > 0) {
          const missingFile = `${translationsDirPath}/_missing_translations_${langCode}.json`;
          await saveJsonFile(missing, missingFile);
          console.log(
            `Generated ${missingFile} with ${
              Object.keys(missing).length
            } missing translations`
          );
        } else {
          console.log(`No missing translations in ${transFile}`);
        }
      } catch (e) {
        console.log(`Error processing ${transFile}: ${e}`);
      }
    }
  } catch (e) {
    console.log(`Error: ${e}`);
  }
}

async function loadJsonFile(filePath) {
  const content = await fs.readFile(filePath, "utf8");
  return JSON.parse(content) || {};
}

async function saveJsonFile(data, filePath) {
  const jsonContent = JSON.stringify(data, null, 2);
  await fs.writeFile(filePath, jsonContent, "utf8");
}

function findMissingTranslations(baseDict, compareDict) {
  const missing = {};

  for (const key of Object.keys(baseDict)) {
    if (!Object.prototype.hasOwnProperty.call(compareDict, key)) {
      missing[key] = baseDict[key];
    }
  }

  return missing;
}

main();
