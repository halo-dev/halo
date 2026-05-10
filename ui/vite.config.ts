import fs from "node:fs";
import path, { resolve } from "node:path";
import { fileURLToPath } from "node:url";
import VueI18n from "@intlify/unplugin-vue-i18n/vite";
import Vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import Gzip from "rollup-plugin-gzip";
import Icons from "unplugin-icons/vite";
import { defineConfig } from "vite-plus";
import { configDefaults } from "vite-plus";
import { setupLibraryExternal } from "./src/vite/library-external.ts";
import { devPlugin } from "./src/vite/plugin-dev.ts";

const DEV_SERVER_PORT = 3000;

const __dirname = path.dirname(new URL(import.meta.url).pathname);
const command = process.env.VP_COMMAND;
const isBuild = command === "build";
const isTest = command === "test";

export default defineConfig({
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
    setupLibraryExternal(command),
    devPlugin({ port: DEV_SERVER_PORT }),
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
      strict: isBuild,
    },
  },
  build: {
    outDir: path.resolve(__dirname, "build/dist/ui"),
    emptyOutDir: true,
    assetsDir: "ui-assets",
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
  fmt: {
    sortTailwindcss: {},
    sortImports: {
      newlinesBetween: false,
    },
    trailingComma: "es5",
    printWidth: 80,
    sortPackageJson: true,
    tabWidth: 2,
    useTabs: false,
    insertFinalNewline: true,
    ignorePatterns: [
      "packages/api-client/src",
      "docs",
      "**/dist/**",
      "build/**",
      "storybook-static",
      ".idea",
    ],
    overrides: [
      {
        files: ["**/*.html"],
        options: {
          printWidth: 120,
        },
      },
    ],
  },
});
