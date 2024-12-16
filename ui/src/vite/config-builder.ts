import Vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import fs from "fs";
import path from "path";
import GzipPlugin from "rollup-plugin-gzip";
import Icons from "unplugin-icons/vite";
import { fileURLToPath } from "url";
import { defineConfig, type Plugin } from "vite";
import { VitePWA } from "vite-plugin-pwa";
import { setupLibraryExternal } from "./library-external";

import legacy from "@vitejs/plugin-legacy";

interface Options {
  base: string;
  entryFile: string;
  port: number;
  outDir: string;
  plugins?: Plugin[];
  mode: string;
}

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
  legacy({
    targets: ["defaults", "not IE 11"],
    polyfills: ["es/object/has-own"],
    modernPolyfills: ["es/object/has-own"],
  }),
];

export function createViteConfig(options: Options) {
  const isProduction = options.mode === "production";

  const { base, entryFile, port, outDir, plugins } = options;

  const currentFileDir = path.dirname(fileURLToPath(import.meta.url));
  const rootDir = path.resolve(currentFileDir, "../..");

  return defineConfig({
    base,
    plugins: [
      ...sharedPlugins,
      ...setupLibraryExternal(isProduction, base, entryFile),
      ...(plugins || []),
    ],
    resolve: {
      alias: {
        "@": path.resolve(rootDir, "src"),
        "@console": path.resolve(rootDir, "console-src"),
        "@uc": path.resolve(rootDir, "uc-src"),
      },
    },
    server: {
      port,
      fs: {
        strict: isProduction ? true : false,
      },
    },
    build: {
      outDir: path.resolve(rootDir, outDir),
      emptyOutDir: true,
      chunkSizeWarningLimit: 2048,
      rollupOptions: {
        output: {
          manualChunks: {
            vendor: [
              "lodash-es",
              "vue-grid-layout",
              "transliteration",
              "vue-draggable-plus",
              "emoji-mart",
              "colorjs.io",
              "jsencrypt",
              "overlayscrollbars",
              "overlayscrollbars-vue",
              "floating-vue",
            ],
          },
        },
      },
    },
  });
}
