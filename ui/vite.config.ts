import fs from "node:fs";
import path, { resolve } from "node:path";
import { fileURLToPath } from "node:url";
import VueI18n from "@intlify/unplugin-vue-i18n/vite";
import Vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import Gzip from "rollup-plugin-gzip";
import Icons from "unplugin-icons/vite";
import { defineConfig } from "vite";
import { configDefaults } from "vitest/config";
import { setupLibraryExternal } from "./src/vite/library-external";

const DEV_SERVER_PORT = 3000;
const DEV_SERVER_ORIGIN = `http://localhost:${DEV_SERVER_PORT}`;

export default defineConfig(({ mode }) => {
  const isProduction = mode === "production";
  const isTest = mode === "test" || Boolean(process.env.VITEST);

  return {
    experimental: {
      bundledDev: !isTest,
    },
    plugins: [
      Vue(),
      VueJsx(),
      Gzip(),
      Icons({
        compiler: "vue3",
        customCollections: {
          core: {
            logo: () => fs.readFileSync("./src/assets/logo.svg", "utf-8"),
          },
        },
      }),
      VueI18n({
        include: [path.resolve(__dirname, "./src/locales/*.json")],
      }),
      ...(!isTest ? setupLibraryExternal(isProduction) : []),
      !isProduction && {
        name: "vite-dev-absolute-urls",
        transformIndexHtml: {
          order: "post" as const,
          handler: (html: string) =>
            html.replace(
              / (src|href)="(\/.+?)"/g,
              ` $1="${DEV_SERVER_ORIGIN}$2"`
            ),
        },
      },
    ],
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "src"),
        "@console": path.resolve(__dirname, "console-src"),
        "@uc": path.resolve(__dirname, "uc-src"),
      },
    },
    server: {
      port: DEV_SERVER_PORT,
      fs: {
        strict: isProduction,
      },
    },
    build: {
      outDir: path.resolve(__dirname, "build/dist/ui"),
      emptyOutDir: true,
      assetsDir: "ui-assets",
      minify: false,
      rolldownOptions: {
        input: {
          console: resolve(import.meta.dirname, "console.html"),
          uc: resolve(import.meta.dirname, "uc.html"),
        },
        output: {
          codeSplitting: {
            groups: [
              {
                name: "vendor",
                test: /node_modules/,
                entriesAware: true,
                entriesAwareMergeThreshold: 28000, // bytes
              },
            ],
          },
        },
      },
    },
    test: {
      environment: "jsdom",
      include: ["**/*.spec.ts"],
      root: fileURLToPath(new URL("./", import.meta.url)),
      exclude: [...configDefaults.exclude, "./packages/**/*.ts"],
      reporters: "html",
      outputFile: "build/test-result/index.html",
    },
  };
});
