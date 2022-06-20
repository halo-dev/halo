import { fileURLToPath, URL } from "url";
import { defineConfig } from "vite";
import Vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import VueSetupExtend from "vite-plugin-vue-setup-extend";
import Compression from "vite-compression-plugin";
import { VitePWA } from "vite-plugin-pwa";
import { viteExternalsPlugin as ViteExternals } from "vite-plugin-externals";
import { viteStaticCopy as ViteStaticCopy } from "vite-plugin-static-copy";

export default ({ mode }: { mode: string }) => {
  const isProduction = mode === "production";

  return defineConfig({
    plugins: [
      Vue(),
      VueJsx(),
      VueSetupExtend(),
      Compression(),
      ViteExternals({
        vue: "Vue",
        "vue-router": "VueRouter",
        "@halo-dev/shared": "HaloAdminShared",
        "@halo-dev/components": "HaloComponents",
      }),
      ViteStaticCopy({
        targets: [
          {
            src: `./node_modules/vue/dist/vue.global${
              isProduction ? ".prod" : ""
            }.js`,
            dest: "assets/vue",
            rename: "vue.global.js",
          },
          {
            src: `./node_modules/vue-router/dist/vue-router.global${
              isProduction ? ".prod" : ""
            }.js`,
            dest: "assets/vue-router",
            rename: "vue-router.global.js",
          },
          {
            src: "./node_modules/@halo-dev/admin-shared/dist/halo-admin-shared.iife.js",
            dest: "assets/admin-shared",
          },
          {
            src: "./node_modules/@halo-dev/components/dist/halo-components.iife.js",
            dest: "assets/components",
          },
        ],
      }),
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
};
