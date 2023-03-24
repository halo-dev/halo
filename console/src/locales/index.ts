import type { App } from "vue";
import { createI18n } from "vue-i18n";
// @ts-ignore
import en from "./en.yaml";
// @ts-ignore
import zhCN from "./zh-CN.yaml";

const messages = {
  en: en,
  zh: zhCN,
  "en-US": en,
  "zh-CN": zhCN,
};

const i18n = createI18n({
  legacy: false,
  locale: "zh-CN",
  fallbackLocale: "zh-CN",
  messages,
});

export function getBrowserLanguage(): string {
  const browserLanguage = navigator.language;
  const language = messages[browserLanguage]
    ? browserLanguage
    : browserLanguage.split("-")[0];
  return language in messages ? language : "zh-CN";
}

export function setupI18n(app: App) {
  app.use(i18n);
}

export { i18n };
