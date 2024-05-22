import { fileURLToPath, URL } from "url";

import { defineConfig, type Plugin } from "vite";
import Vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import Icons from "unplugin-icons/vite";
import Dts from "vite-plugin-dts";
import path from "path";

export default defineConfig({
  plugins: [
    Vue(),
    VueJsx(),
    Icons({ compiler: "vue3" }),
    Dts({
      tsconfigPath: "./tsconfig.app.json",
      entryRoot: "./src",
      outDir: "./dist",
      insertTypesEntry: true,
    }) as Plugin,
  ],
  define: {
    "process.env": process.env,
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
      fileName: (format) => `halo-components.${format}.js`,
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
        generatedCode: "es5",
      },
    },
    sourcemap: true,
  },
});
