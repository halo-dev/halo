import path from "node:path";
import { fileURLToPath, URL } from "node:url";
import Vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import Dts from "unplugin-dts/vite";
import Icons from "unplugin-icons/vite";
import { defineConfig, type Plugin } from "vite-plus";
import { configDefaults } from "vite-plus";

export default defineConfig({
  plugins: [
    Vue(),
    VueJsx(),
    Icons({ compiler: "vue3" }),
    Dts({
      processor: "vue",
      tsconfigPath: "./tsconfig.app.json",
      entryRoot: "./src",
      outDirs: "./dist",
      insertTypesEntry: true,
    }) as Plugin,
  ],
  define: {
    "process.env.NODE_ENV": '"production"',
  },
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  build: {
    outDir: path.resolve(__dirname, "dist"),
    lib: {
      entry: path.resolve(__dirname, "src/index.ts"),
      name: "HaloComponents",
      formats: ["es", "iife"],
      fileName: (format) => `index.${format}.js`,
      cssFileName: "style",
    },
    rollupOptions: {
      external: [
        "vue",
        "vue-router",
        "@vueuse/core",
        "@vueuse/components",
        "@vueuse/router",
      ],
      output: {
        globals: {
          vue: "Vue",
          "vue-router": "VueRouter",
          "@vueuse/core": "VueUse",
          "@vueuse/components": "VueUse",
          "@vueuse/router": "VueUse",
        },
        exports: "named",
      },
    },
    sourcemap: true,
  },
  test: {
    // Vitest v4 compatibility: preserve mock call history.
    // Remove after tests no longer rely on calls from setup or earlier tests.
    // https://viteplus.dev/guide/vitest-v5#remove-unneeded-compatibility-settings
    // https://vitest.dev/guide/migration/#clearmocks-is-enabled-by-default
    clearMocks: false,
    environment: "jsdom",
    exclude: [...configDefaults.exclude],
    root: fileURLToPath(new URL("./", import.meta.url)),
  },
});
