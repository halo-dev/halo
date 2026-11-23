import { shallowRef } from "vue";
import en from "./en.json";
import es from "./es.json";
import zhCN from "./zh-CN.json";
import zhTW from "./zh-TW.json";

export type ComponentsLocale = "en" | "zh-CN" | "zh-TW" | "es";

const messages: Record<ComponentsLocale, Record<string, string>> = {
  en,
  "zh-CN": zhCN,
  "zh-TW": zhTW,
  es,
};

const currentLocale = shallowRef<ComponentsLocale>("en");

export function resolveComponentsLocale(locale?: string): ComponentsLocale {
  if (!locale) {
    return "en";
  }
  if (Object.prototype.hasOwnProperty.call(messages, locale)) {
    return locale as ComponentsLocale;
  }
  const normalized = locale.toLowerCase();
  if (normalized.startsWith("zh")) {
    return /hant|tw|hk|mo/.test(normalized) ? "zh-TW" : "zh-CN";
  }
  if (normalized.startsWith("es")) {
    return "es";
  }
  return "en";
}

export function setComponentsLocale(locale?: string): void {
  currentLocale.value = resolveComponentsLocale(locale);
}

export function getComponentsLocale(): ComponentsLocale {
  return currentLocale.value;
}

function lookup(locale: ComponentsLocale, key: string): string | undefined {
  const table = messages[locale];
  return Object.prototype.hasOwnProperty.call(table, key)
    ? table[key]
    : undefined;
}

export function translate(
  key: string,
  params?: Record<string, string | number>
): string {
  const message = lookup(currentLocale.value, key) ?? lookup("en", key) ?? key;
  if (!params) {
    return message;
  }
  return message.replace(/\{(\w+)\}/g, (match, name: string) =>
    name in params ? String(params[name]) : match
  );
}

export function useComponentsI18n() {
  return {
    locale: currentLocale,
    t: translate,
  };
}
