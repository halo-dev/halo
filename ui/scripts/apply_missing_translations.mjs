/**
 * Apply Missing Translations
 * -------------------------
 * This script applies translated entries from "_missing_translations_[lang].yaml" files
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
 * Processing: src/locales/_missing_translations_zh-TW.yaml for language: zh-TW
 * Updated src/locales/zh-TW.yaml with 15 translated entries.
 * Updated src/locales/_missing_translations_zh-TW.yaml with 10 remaining untranslated entries.
 *
 * This script is designed to be run repeatedly as you translate more entries in the
 * missing translations files. It will only apply entries that differ from the English version.
 */

import { existsSync } from "fs";
import fs from "fs/promises";
import yaml from "js-yaml";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const translationsDirPath = path.resolve(__dirname, "../src/locales");
const baseFile = path.join(translationsDirPath, "en.yaml");

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
          entry.name.endsWith(".yaml")
      )
      .map((entry) => path.join(translationsDirPath, entry.name));

    if (missingFiles.length === 0) {
      console.log("No missing translation files found.");
      return;
    }

    const enTranslations = await loadYamlFile(baseFile);

    for (const missingFile of missingFiles) {
      const fileName = path.basename(missingFile, ".yaml");
      const langCode = fileName.replace("_missing_translations_", "");
      const targetFile = path.join(translationsDirPath, `${langCode}.yaml`);

      console.log(`\nProcessing: ${missingFile} for language: ${langCode}`);

      if (!existsSync(targetFile)) {
        console.log(`Target translation file ${targetFile} does not exist`);
        continue;
      }

      try {
        const missingTranslations = await loadYamlFile(missingFile);
        const currentTranslations = await loadYamlFile(targetFile);

        const translatedEntries = {};
        const untranslatedEntries = {};
        const stats = { added: 0, skipped: 0 };

        const keyPaths = collectKeyPaths(missingTranslations);
        console.log(
          `Found ${keyPaths.length} keys in missing translations file.`
        );

        for (const keyPath of keyPaths) {
          const missingValue = getValueByPath(missingTranslations, keyPath);
          const enValue = getValueByPath(enTranslations, keyPath);

          if (
            missingValue !== enValue &&
            missingValue !== null &&
            missingValue !== undefined
          ) {
            setValueByPath(translatedEntries, keyPath, missingValue);
            stats.added++;
            if (VERBOSE) {
              console.log(
                `✓ TRANSLATED: ${keyPath.join(
                  "."
                )} = "${missingValue}" (EN: "${enValue}")`
              );
            }
          } else {
            setValueByPath(untranslatedEntries, keyPath, missingValue);
            stats.skipped++;
            if (VERBOSE) {
              console.log(
                `✗ NOT TRANSLATED: ${keyPath.join(
                  "."
                )} = "${missingValue}" (same as EN: "${enValue}")`
              );
            }
          }
        }

        if (stats.added > 0) {
          const updatedTranslations = deepMerge(
            currentTranslations,
            translatedEntries
          );

          await saveYamlFile(updatedTranslations, targetFile);
          console.log(
            `Updated ${targetFile} with ${stats.added} translated entries.`
          );

          await saveYamlFile(untranslatedEntries, missingFile);
          console.log(
            `Updated ${missingFile} with ${stats.skipped} remaining untranslated entries.`
          );
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

async function loadYamlFile(filePath) {
  try {
    const content = await fs.readFile(filePath, "utf8");
    return yaml.load(content) || {};
  } catch (error) {
    console.error(`Error loading file ${filePath}:`, error);
    return {};
  }
}

async function saveYamlFile(data, filePath) {
  try {
    const yamlContent = yaml.dump(data, {
      indent: 2,
      lineWidth: -1,
    });
    await fs.writeFile(filePath, yamlContent, "utf8");
    return true;
  } catch (error) {
    console.error(`Error saving file ${filePath}:`, error);
    return false;
  }
}

function collectKeyPaths(obj, currentPath = [], result = []) {
  if (obj === null || typeof obj !== "object") {
    return result;
  }

  for (const key of Object.keys(obj)) {
    const newPath = [...currentPath, key];

    if (obj[key] === null || typeof obj[key] !== "object") {
      result.push(newPath);
    } else {
      collectKeyPaths(obj[key], newPath, result);
    }
  }

  return result;
}

function getValueByPath(obj, path) {
  let current = obj;

  for (const key of path) {
    if (
      current === null ||
      current === undefined ||
      typeof current !== "object"
    ) {
      return undefined;
    }
    current = current[key];
  }

  return current;
}

function setValueByPath(obj, path, value) {
  let current = obj;

  for (let i = 0; i < path.length - 1; i++) {
    const key = path[i];

    if (!current[key] || typeof current[key] !== "object") {
      current[key] = {};
    }

    current = current[key];
  }

  const lastKey = path[path.length - 1];
  current[lastKey] = value;
}

function deepMerge(target, source) {
  const result = { ...target };

  for (const key of Object.keys(source)) {
    if (source[key] === null || typeof source[key] !== "object") {
      result[key] = source[key];
    } else if (target[key] === null || typeof target[key] !== "object") {
      result[key] = { ...source[key] };
    } else {
      result[key] = deepMerge(target[key], source[key]);
    }
  }

  return result;
}

main();
