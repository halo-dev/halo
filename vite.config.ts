import { fileURLToPath, URL } from "url";

import { defineConfig } from "vite";
import Vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import VueSetupExtend from "vite-plugin-vue-setup-extend";
import Compression from "vite-compression-plugin";
import { VitePWA } from "vite-plugin-pwa";

export default defineConfig({
  plugins: [
    Vue(),
    VueJsx(),
    VueSetupExtend(),
    Compression(),
    VitePWA({
      manifest: {
        name: "Halo",
        short_name: "Halo",
        description: "Web Client For Halo",
        theme_color: "#fff",
      },
    }),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  build: {
    chunkSizeWarningLimit: 2048,
  },
  test: {
    transformMode: {
      web: [/\.[jt]sx$/],
    },
  },
});
