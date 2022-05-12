import { fileURLToPath, URL } from "url";

import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vueJsx from "@vitejs/plugin-vue-jsx";
import icons from "unplugin-icons/vite";
import Compression from "vite-compression-plugin";
import { VitePWA } from "vite-plugin-pwa";
import dts from "vite-plugin-dts";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
    icons(),
    Compression(),
    VitePWA({
      manifest: {
        name: "Halo",
        short_name: "Halo",
        description: "Web Client For Halo",
        theme_color: "#fff",
      },
    }),
    dts({
      outputDir: "dist-typings",
      entryRoot: "./src",
      staticImport: true,
    }),
  ],
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
