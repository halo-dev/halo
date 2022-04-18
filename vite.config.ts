import { fileURLToPath, URL } from "url";

import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vueJsx from "@vitejs/plugin-vue-jsx";
import icons from "unplugin-icons/vite";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue(), vueJsx(), icons()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  test: {
    transformMode: {
      web: [/\.[jt]sx$/],
    },
  },
});
