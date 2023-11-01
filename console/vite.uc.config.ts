import { fileURLToPath, URL } from "url";
import fs from "fs";
import path from "path";
import { defineConfig, Plugin } from "vite";
import Vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import { VitePWA } from "vite-plugin-pwa";
import Icons from "unplugin-icons/vite";
import { setupLibraryExternal } from "./src/build/library-external";
import VueI18nPlugin from "@intlify/unplugin-vue-i18n/vite";
import GzipPlugin from "rollup-plugin-gzip";

export const sharedPlugins = [
  Vue({
    script: {
      defineModel: true,
    },
  }),
  VueJsx(),
  GzipPlugin() as Plugin,
  Icons({
    compiler: "vue3",
    customCollections: {
      core: {
        logo: () => fs.readFileSync("./src/assets/logo.svg", "utf-8"),
      },
    },
  }),
  VitePWA({
    manifest: {
      name: "Halo",
      short_name: "Halo",
      description: "Web Client For Halo",
      theme_color: "#fff",
    },
    disable: true,
  }),
  VueI18nPlugin({
    include: [path.resolve(__dirname, "./src/locales/*.yaml")],
  }),
];

export default ({ mode }: { mode: string }) => {
  const isProduction = mode === "production";

  return defineConfig({
    base: "/uc/",
    plugins: [
      ...sharedPlugins,
      ...setupLibraryExternal(isProduction, "/uc/", "/uc-src/main.ts"),
    ],
    resolve: {
      alias: {
        "@": fileURLToPath(new URL("./src", import.meta.url)),
        "@uc": fileURLToPath(new URL("./uc-src", import.meta.url)),
      },
    },
    server: {
      port: 4000,
      fs: {
        strict: isProduction ? true : false,
      },
    },
    build: {
      outDir: fileURLToPath(
        new URL("../application/src/main/resources/uc", import.meta.url)
      ),
      emptyOutDir: true,
      chunkSizeWarningLimit: 2048,
    },
  });
};
