import { createI18n } from "vue-i18n";
import en from "./en.json";
import es from "./es.json";
import zhCN from "./zh-CN.json";

const messages = {
  en: en,
  zh: zhCN,
  es: es,
  "en-US": en,
  "zh-CN": zhCN,
  "es-ES": es,
};

const i18n = createI18n({
  legacy: false,
  locale: "en",
  fallbackLocale: "en",
  messages,
});

export { i18n };
