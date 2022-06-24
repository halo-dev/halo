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
      name: "HaloAdminShared",
      formats: ["es", "cjs", "umd", "iife"],
      fileName: (format) => `halo-admin-shared.${format}.js`,
    },
    rollupOptions: {
      external: ["vue", "vue-router", "@halo-dev/components"],
      output: {
        globals: {
          vue: "Vue",
          "vue-router": "VueRouter",
          "@halo-dev/components": "HaloComponents",
        },
        exports: "named",
      },
    },
    sourcemap: true,
  },
});
