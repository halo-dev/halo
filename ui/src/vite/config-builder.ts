import Vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import fs from "fs";
import path from "path";
import GzipPlugin from "rollup-plugin-gzip";
import Icons from "unplugin-icons/vite";
import { fileURLToPath } from "url";
import { defineConfig, type Plugin } from "vite";
import { setupLibraryExternal } from "./library-external";

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
];

export function createViteConfig(options: Options) {
  const isProduction = options.mode === "production";

  const { base, entryFile, port, outDir, plugins } = options;

  const currentFileDir = path.dirname(fileURLToPath(import.meta.url));
  const rootDir = path.resolve(currentFileDir, "../..");

  return defineConfig({
    base,
    experimental: {
      enableNativePlugin: isProduction,
    },
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
          advancedChunks: {
            groups: [
              "lodash-es",
              "vue-grid-layout",
              "transliteration",
              "vue-draggable-plus",
              "colorjs.io",
              "overlayscrollbars",
              "overlayscrollbars-vue",
              "floating-vue",
              "@he-tree/vue",
              "pretty-bytes",
            ].map((name) => ({
              name: "vendor",
              test: name,
            })),
          },
        },
      },
    },
  });
}
