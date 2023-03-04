import { fileURLToPath, URL } from "url";
import fs from "fs";
import { defineConfig, loadEnv } from "vite";
import Vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import Compression from "vite-compression-plugin";
import { VitePWA } from "vite-plugin-pwa";
import Icons from "unplugin-icons/vite";
import { setupLibraryExternal } from "./src/build/library-external";

export const sharedPlugins = [
  Vue(),
  VueJsx(),
  Compression(),
  Icons({
    compiler: "vue3",
    customCollections: {
      core: {
        logo: () => fs.readFileSync("./src/assets/logo.svg", "utf-8"),
      },
    },
  }),
  VitePWA({
    manifest: {
      name: "Halo",
      short_name: "Halo",
      description: "Web Client For Halo",
      theme_color: "#fff",
    },
    disable: true,
  }),
];

export default ({ mode }: { mode: string }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const isProduction = mode === "production";

  return defineConfig({
    base: env.VITE_BASE_URL,
    plugins: [
      ...sharedPlugins,
      ...setupLibraryExternal(isProduction, env.VITE_BASE_URL),
    ],
    resolve: {
      alias: {
        "@": fileURLToPath(new URL("./src", import.meta.url)),
      },
    },
    server: {
      port: 3000,
    },
    build: {
      outDir: fileURLToPath(
        new URL("../src/main/resources/console", import.meta.url)
      ),
      emptyOutDir: true,
      chunkSizeWarningLimit: 2048,
    },
  });
};
