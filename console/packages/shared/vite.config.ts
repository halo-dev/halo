import { fileURLToPath, URL } from "url";

import { defineConfig } from "vite";
import Vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import path from "path";
import Dts from "vite-plugin-dts";

export default defineConfig({
  plugins: [
    Vue(),
    VueJsx(),
    Dts({
      entryRoot: "./src",
      outputDir: "./dist",
      insertTypesEntry: true,
    }),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  build: {
    outDir: path.resolve(__dirname, "dist"),
    lib: {
      entry: path.resolve(__dirname, "src/index.ts"),
      name: "HaloConsoleShared",
      formats: ["es", "iife"],
      fileName: (format) => `halo-console-shared.${format}.js`,
    },
    rollupOptions: {
      external: ["vue", "vue-router"],
      output: {
        globals: {
          vue: "Vue",
          "vue-router": "VueRouter",
        },
        exports: "named",
        generatedCode: "es5",
      },
    },
    sourcemap: true,
  },
});
