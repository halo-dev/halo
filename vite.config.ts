import { fileURLToPath, URL } from "url";
import { defineConfig, loadEnv } from "vite";
import Vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import VueSetupExtend from "vite-plugin-vue-setup-extend";
import Compression from "vite-compression-plugin";
import { VitePWA } from "vite-plugin-pwa";
import Icons from "unplugin-icons/vite";
import { setupLibraryExternal } from "./src/build/library-external";

export default ({ mode }: { mode: string }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const isProduction = mode === "production";

  return defineConfig({
    base: env.VITE_BASE_URL,
    plugins: [
      Vue(),
      VueJsx(),
      VueSetupExtend(),
      Compression(),
      Icons({ compiler: "vue3" }),
      VitePWA({
        manifest: {
          name: "Halo",
          short_name: "Halo",
          description: "Web Client For Halo",
          theme_color: "#fff",
        },
        disable: true,
      }),
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
      chunkSizeWarningLimit: 2048,
    },
  });
};
