/**
 * Apply Missing Translations
 * -------------------------
 * This script applies translated entries from "_missing_translations_[lang].json" files
 * to their corresponding language files.
 *
 * For each missing translations file, it:
 * 1. Compares entries with the English base file
 * 2. Identifies which entries have been translated (values different from English)
 * 3. Merges only the translated entries into the main language file
 * 4. Updates the missing translations file to keep only untranslated entries
 *
 * Usage:
 * node scripts/apply_missing_translations.mjs
 *
 * Example output:
 * Processing: src/locales/_missing_translations_zh-TW.json for language: zh-TW
 * Updated src/locales/zh-TW.json with 15 translated entries.
 * Updated src/locales/_missing_translations_zh-TW.json with 10 remaining untranslated entries.
 *
 * This script is designed to be run repeatedly as you translate more entries in the
 * missing translations files. It will only apply entries that differ from the English version.
 */

import { existsSync } from "fs";
import fs from "fs/promises";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const translationsDirPath = path.resolve(__dirname, "../src/locales");
const baseFile = path.join(translationsDirPath, "en.json");

const VERBOSE = true;

async function main() {
  try {
    const dirEntries = await fs.readdir(translationsDirPath, {
      withFileTypes: true,
    });

    const missingFiles = dirEntries
      .filter(
        (entry) =>
          entry.isFile() &&
          entry.name.includes("_missing_translations_") &&
          entry.name.endsWith(".json")
      )
      .map((entry) => path.join(translationsDirPath, entry.name));

    if (missingFiles.length === 0) {
      console.log("No missing translation files found.");
      return;
    }

    const enTranslations = await loadJsonFile(baseFile);

    for (const missingFile of missingFiles) {
      const fileName = path.basename(missingFile, ".json");
      const langCode = fileName.replace("_missing_translations_", "");
      const targetFile = path.join(translationsDirPath, `${langCode}.json`);

      console.log(`\nProcessing: ${missingFile} for language: ${langCode}`);

      if (!existsSync(targetFile)) {
        console.log(`Target translation file ${targetFile} does not exist`);
        continue;
      }

      try {
        const missingTranslations = await loadJsonFile(missingFile);
        const currentTranslations = await loadJsonFile(targetFile);

        const translatedEntries = {};
        const untranslatedEntries = {};
        const stats = { added: 0, skipped: 0 };

        const keys = Object.keys(missingTranslations);
        console.log(`Found ${keys.length} keys in missing translations file.`);

        for (const key of keys) {
          const missingValue = missingTranslations[key];
          const enValue = enTranslations[key];

          if (
            missingValue !== enValue &&
            missingValue !== null &&
            missingValue !== undefined
          ) {
            translatedEntries[key] = missingValue;
            stats.added++;
            if (VERBOSE) {
              console.log(
                `✓ TRANSLATED: ${key} = "${missingValue}" (EN: "${enValue}")`
              );
            }
          } else {
            untranslatedEntries[key] = missingValue;
            stats.skipped++;
            if (VERBOSE) {
              console.log(
                `✗ NOT TRANSLATED: ${key} = "${missingValue}" (same as EN: "${enValue}")`
              );
            }
          }
        }

        if (stats.added > 0) {
          const updatedTranslations = {
            ...currentTranslations,
            ...translatedEntries,
          };

          await saveJsonFile(updatedTranslations, targetFile);
          console.log(
            `Updated ${targetFile} with ${stats.added} translated entries.`
          );

          if (stats.skipped > 0) {
            await saveJsonFile(untranslatedEntries, missingFile);
            console.log(
              `Updated ${missingFile} with ${stats.skipped} remaining untranslated entries.`
            );
          } else {
            // Delete the missing file if all entries are translated
            await fs.unlink(missingFile);
            console.log(
              `Deleted ${missingFile} as all entries are now translated.`
            );
          }
        } else {
          console.log(
            `No translated entries found in ${missingFile}, files not updated.`
          );
        }

        console.log(`\nSummary for ${langCode}:`);
        console.log(`- Added: ${stats.added} translated entries`);
        console.log(`- Remaining: ${stats.skipped} untranslated entries`);
      } catch (e) {
        console.error(`Error processing ${missingFile}:`, e);
      }
    }
  } catch (e) {
    console.error(`Error:`, e);
  }
}

async function loadJsonFile(filePath) {
  try {
    const content = await fs.readFile(filePath, "utf8");
    return JSON.parse(content) || {};
  } catch (error) {
    console.error(`Error loading file ${filePath}:`, error);
    return {};
  }
}

async function saveJsonFile(data, filePath) {
  try {
    const jsonContent = JSON.stringify(data, null, 2);
    await fs.writeFile(filePath, jsonContent, "utf8");
    return true;
  } catch (error) {
    console.error(`Error saving file ${filePath}:`, error);
    return false;
  }
}

main();
