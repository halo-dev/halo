import path from "path";
import VueI18nPlugin from "@intlify/unplugin-vue-i18n/vite";
import { createViteConfig } from "./src/vite/config-builder";
import { Plugin } from "vite";

export default ({ mode }: { mode: string }) => {
  return createViteConfig({
    base: "/uc/",
    entryFile: "/uc-src/main.ts",
    port: 4000,
    outDir: path.resolve("../application/src/main/resources/uc"),
    mode,
    plugins: [
      VueI18nPlugin({
        include: [path.resolve(__dirname, "./src/locales/*.yaml")],
      }) as Plugin,
    ],
  });
};
