import type { App } from "vue";
import { createI18n } from "vue-i18n";
// @ts-ignore
import en from "./en.yaml";
// @ts-ignore
import zhCN from "./zh-CN.yaml";

export const locales = [
  {
    code: "en",
    package: en,
    hidden: true,
  },
  {
    name: "English",
    code: "en-US",
    package: en,
  },
  {
    name: "简体中文",
    code: "zh-CN",
    package: zhCN,
  },
  {
    code: "zh",
    package: zhCN,
  },
];

const messages = locales.reduce((acc, cur) => {
  acc[cur.code] = cur.package;
  return acc;
}, {});

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
