import type { Plugin as HaloPlugin } from "@halo-dev/api-client";
import fs from "fs";
import yaml from "js-yaml";
import { Plugin } from "vite";

export function HaloUIPluginBundlerKit(): Plugin {
  return {
    name: "halo-ui-plugin-bundler-kit",
    config(config, env) {
      const isProduction = env.mode === "production";

      // fixme: allow user to config outDir
      const outDir = isProduction
        ? "../src/main/resources/console"
        : "../build/resources/main/console";

      // fixme: allow user to config manifest path
      const manifest = yaml.load(
        fs.readFileSync("../src/main/resources/plugin.yaml", "utf8")
      ) as HaloPlugin;

      return {
        ...config,
        define: {
          "process.env": process.env,
        },
        build: {
          outDir,
          emptyOutDir: true,
          lib: {
            entry: "src/index.ts",
            name: manifest.metadata.name,
            formats: ["iife"],
            fileName: () => "main.js",
          },
          rollupOptions: {
            external: [
              "vue",
              "vue-router",
              "@vueuse/core",
              "@vueuse/components",
              "@vueuse/router",
              "@halo-dev/shared",
              "@halo-dev/components",
              "@halo-dev/api-client",
              "@halo-dev/richtext-editor",
              "axios",
            ],
            output: {
              globals: {
                vue: "Vue",
                "vue-router": "VueRouter",
                "@vueuse/core": "VueUse",
                "@vueuse/components": "VueUse",
                "@vueuse/router": "VueUse",
                "@halo-dev/console-shared": "HaloConsoleShared",
                "@halo-dev/components": "HaloComponents",
                "@halo-dev/api-client": "HaloApiClient",
                "@halo-dev/richtext-editor": "RichTextEditor",
                axios: "axios",
              },
              extend: true,
            },
          },
        },
      };
    },
  };
}
