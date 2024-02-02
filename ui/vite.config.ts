import path from "path";
import { loadEnv, Plugin } from "vite";
import VueI18nPlugin from "@intlify/unplugin-vue-i18n/vite";
import { createViteConfig } from "./src/vite/config-builder";

export default ({ mode }: { mode: string }) => {
  const env = loadEnv(mode, process.cwd(), "");
  return createViteConfig({
    base: env.VITE_BASE_URL,
    entryFile: "/console-src/main.ts",
    port: 3000,
    outDir: path.resolve("../application/src/main/resources/console"),
    mode,
    plugins: [
      VueI18nPlugin({
        include: [path.resolve(__dirname, "./src/locales/*.yaml")],
      }) as Plugin,
    ],
  });
};
