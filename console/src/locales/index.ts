import { createI18n } from "vue-i18n";
import zh from "./lang/zh";

const messages = {
  zh,
};

const i18n = createI18n({
  legacy: false,
  locale: "zh",
  messages,
});

export default i18n;
