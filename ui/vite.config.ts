import path from "path";
import { Plugin } from "vite";
import VueI18nPlugin from "@intlify/unplugin-vue-i18n/vite";
import { createViteConfig } from "./src/vite/config-builder";

export default ({ mode }: { mode: string }) => {
  return createViteConfig({
    base: "/console/",
    entryFile: "/console-src/main.ts",
    port: 3000,
    outDir: path.resolve("build/dist/console"),
    mode,
    plugins: [
      VueI18nPlugin({
        include: [path.resolve(__dirname, "./src/locales/*.yaml")],
      }) as Plugin,
    ],
  });
};
