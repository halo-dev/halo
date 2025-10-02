import { fileURLToPath, URL } from "url";

import Vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import path from "path";
import Icons from "unplugin-icons/vite";
import { defineConfig, type Plugin } from "vite";
import Dts from "vite-plugin-dts";

export default defineConfig({
  experimental: {
    enableNativePlugin: false,
  },
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
      fileName: (format) => `halo-components.${format}.js`,
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
});
