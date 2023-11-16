import { fileURLToPath, URL } from "url";
import fs from "fs";
import { defineConfig, type Plugin } from "vite";
import Vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import { VitePWA } from "vite-plugin-pwa";
import Icons from "unplugin-icons/vite";
import { setupLibraryExternal } from "./library-external";
import GzipPlugin from "rollup-plugin-gzip";

interface Options {
  base: string;
  entryFile: string;
  port: number;
  outDir: string;
  plugins?: Plugin[];
  mode: string;
}

export const sharedPlugins = [
  Vue({
    script: {
      defineModel: true,
    },
  }),
  VueJsx(),
  GzipPlugin() as Plugin,
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

export function createViteConfig(options: Options) {
  const isProduction = options.mode === "production";

  const { base, entryFile, port, outDir, plugins } = options;

  return defineConfig({
    base,
    plugins: [
      ...sharedPlugins,
      ...setupLibraryExternal(isProduction, base, entryFile),
      ...(plugins || []),
    ],
    resolve: {
      alias: {
        "@": fileURLToPath(new URL("/src", import.meta.url)),
        "@console": fileURLToPath(new URL("/console-src", import.meta.url)),
        "@uc": fileURLToPath(new URL("/uc-src", import.meta.url)),
      },
    },
    server: {
      port,
      fs: {
        strict: isProduction ? true : false,
      },
    },
    build: {
      outDir: fileURLToPath(new URL(outDir, import.meta.url)),
      emptyOutDir: true,
      chunkSizeWarningLimit: 2048,
    },
  });
}
