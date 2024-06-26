import { fileURLToPath, URL } from "node:url";

import VueI18nPlugin from "@intlify/unplugin-vue-i18n/vite";
import Vue from "@vitejs/plugin-vue";
import path from "node:path";
import Icons from "unplugin-icons/vite";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [
    Vue(),
    Icons({ compiler: "vue3" }),
    VueI18nPlugin({
      include: [path.resolve(__dirname, "./src/locales/*.yaml")],
    }),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
});
