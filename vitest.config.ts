import { defineConfig } from "vitest/config";
import Vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import { fileURLToPath, URL } from "url";

export default defineConfig({
  plugins: [Vue(), VueJsx()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  test: {
    dir: "./src",
    transformMode: {
      web: [/\.[jt]sx$/],
    },
  },
});
