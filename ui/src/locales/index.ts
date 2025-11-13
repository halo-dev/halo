import { getCookie } from "@/utils/cookie";
import { utils } from "@halo-dev/ui-shared";
import type { App } from "vue";
import { createI18n } from "vue-i18n";

interface LocaleConfig {
  code: string[];
  file: string;
}

export const SUPPORTED_LOCALES: LocaleConfig[] = [
  {
    code: ["en"],
    file: "en.yaml",
  },
  {
    code: ["es"],
    file: "es.yaml",
  },
  {
    code: ["zh-CN", "zh"],
    file: "zh-CN.yaml",
  },
  {
    code: ["zh-TW"],
    file: "zh-TW.yaml",
  },
];

const localeModules = import.meta.glob<{ default: Record<string, unknown> }>(
  "./*.yaml",
  { eager: false }
);

const i18n = createI18n({
  legacy: false,
  fallbackLocale: "en",
});

export function getEnvironmentLanguage(): string {
  return getCookie("language") || navigator.language;
}

export function getLocaleDefinition(
  language: string
): LocaleConfig | undefined {
  const locale = SUPPORTED_LOCALES.find((locale) =>
    locale.code.includes(language)
  );

  if (locale) {
    return locale;
  }

  const code = language.split("-")[0];
  return SUPPORTED_LOCALES.find((locale) => locale.code.includes(code));
}

export async function setLanguage(_language?: string): Promise<void> {
  const language = _language || getEnvironmentLanguage();

  if (!i18n.global.availableLocales.includes(language)) {
    const locale = getLocaleDefinition(language);
    if (locale) {
      try {
        const localeLoader = localeModules[`./${locale.file}`];
        if (!localeLoader) {
          throw new Error(`Locale file ${locale.file} not found`);
        }
        const messages = await localeLoader();
        i18n.global.setLocaleMessage(language, messages.default || messages);
      } catch (error) {
        console.error(`Failed to load locale file for ${language}:`, error);
        await loadFallbackLocale();
        return;
      }
    } else {
      console.warn(`Locale not found for ${language}, using fallback`);
      await loadFallbackLocale();
      return;
    }
  }

  i18n.global.locale.value = language;
  utils.date.setLocale(language);
}

async function loadFallbackLocale(): Promise<void> {
  const fallback = i18n.global.fallbackLocale.value as string;

  if (!i18n.global.availableLocales.includes(fallback)) {
    const fallbackLocale = getLocaleDefinition(fallback);
    if (fallbackLocale) {
      try {
        const localeLoader = localeModules[`./${fallbackLocale.file}`];
        if (!localeLoader) {
          throw new Error(
            `Fallback locale file ${fallbackLocale.file} not found`
          );
        }
        const messages = await localeLoader();
        i18n.global.setLocaleMessage(fallback, messages.default || messages);
      } catch (error) {
        console.error(`Failed to load fallback locale file:`, error);
      }
    }
  }

  i18n.global.locale.value = fallback;
}

export function setupI18n(app: App): void {
  app.use(i18n);
}

export { i18n };
