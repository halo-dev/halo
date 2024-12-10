import { fileURLToPath, URL } from "url";

import { defineConfig } from "vite";
import Vue from "@vitejs/plugin-vue";
import Icons from "unplugin-icons/vite";
import dts from "vite-plugin-dts";
import path from "path";
import VueI18nPlugin from "@intlify/unplugin-vue-i18n/vite";

export default ({ mode }: { mode: string }) => {
  const isProduction = mode === "production";

  return defineConfig({
    plugins: [
      Vue(),
      Icons({
        compiler: "vue3",
      }),
      isProduction &&
        dts({
          tsconfigPath: "./tsconfig.app.json",
          entryRoot: "./src",
          outDir: "./dist",
          insertTypesEntry: true,
        }),
      VueI18nPlugin({
        include: [path.resolve(__dirname, "./src/locales/*.yaml")],
      }),
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
        name: "RichTextEditor",
        formats: ["es", "iife"],
        fileName: (format) => `rich-text-editor.${format}.js`,
        cssFileName: "style",
      },
      minify: isProduction,
      rollupOptions: {
        external: ["vue"],
        output: {
          globals: {
            vue: "Vue",
          },
          exports: "named",
        },
      },
      sourcemap: false,
    },
  });
};
