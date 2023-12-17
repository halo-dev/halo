import { defineConfig } from "vitest/config";
import { fileURLToPath, URL } from "url";
import VueI18nPlugin from "@intlify/unplugin-vue-i18n/vite";
import { sharedPlugins } from "./src/vite/config-builder";
import path from "path";
import type { Plugin } from "vite";

export default defineConfig({
  plugins: [
    sharedPlugins,
    VueI18nPlugin({
      include: [path.resolve(__dirname, "./src/locales/*.yaml")],
    }) as Plugin,
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
      "@console": fileURLToPath(new URL("./console-src", import.meta.url)),
    },
  },
  test: {
    dir: "./src",
    transformMode: {
      web: [/\.[jt]sx$/],
    },
  },
});
