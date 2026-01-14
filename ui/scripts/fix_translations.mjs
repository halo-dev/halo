/**
 * Fix Translations
 * -------------------------
 * This script removes translation keys that exist in language files but are not
 * present in the English base file (en.json).
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
 * Extra key found: core.common.outdatedKey
 * Removed 5 extra keys from src/locales/zh-TW.json
 *
 * This script helps maintain consistency across language files by ensuring they
 * only contain keys that are present in the English base file.
 */

import fs from "fs/promises";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const translationsDirPath = path.resolve(__dirname, "../src/locales");
const baseFile = path.join(translationsDirPath, "en.json");

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
      try {
        const translations = await loadJsonFile(transFile);

        const extraKeysCount = removeExtraTranslations(
          translations,
          baseTranslations
        );

        if (extraKeysCount > 0) {
          await saveJsonFile(translations, transFile);
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

async function loadJsonFile(filePath) {
  const content = await fs.readFile(filePath, "utf8");
  return JSON.parse(content) || {};
}

async function saveJsonFile(data, filePath) {
  const jsonContent = JSON.stringify(data, null, 2);
  await fs.writeFile(filePath, jsonContent, "utf8");
}

function removeExtraTranslations(translations, baseTranslations) {
  let extraKeysCount = 0;
  const keysToDelete = [];

  for (const key of Object.keys(translations)) {
    if (!Object.prototype.hasOwnProperty.call(baseTranslations, key)) {
      keysToDelete.push(key);
      extraKeysCount++;
      console.log(`Extra key found: ${key}`);
    }
  }

  for (const key of keysToDelete) {
    delete translations[key];
  }

  return extraKeysCount;
}

main();
