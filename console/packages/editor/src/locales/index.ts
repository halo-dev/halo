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
  locale: "en",
  fallbackLocale: "zh-CN",
  messages,
});

export { i18n };
