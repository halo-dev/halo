import type { Plugin as HaloPlugin } from "@halo-dev/api-client";
import fs from "fs";
import yaml from "js-yaml";
import { Plugin } from "vite";

const DEFAULT_OUT_DIR_DEV = "../src/main/resources/console";
const DEFAULT_OUT_DIR_PROD = "../build/resources/main/console";
const DEFAULT_MANIFEST_PATH = "../src/main/resources/plugin.yaml";

interface HaloUIPluginBundlerKitOptions {
  outDir?:
    | string
    | {
        dev: string;
        prod: string;
      };
  manifestPath?: string;
}

export function HaloUIPluginBundlerKit(
  options: HaloUIPluginBundlerKitOptions = {}
): Plugin {
  return {
    name: "halo-ui-plugin-bundler-kit",
    config(config, env) {
      const isProduction = env.mode === "production";

      let outDir = isProduction ? DEFAULT_OUT_DIR_PROD : DEFAULT_OUT_DIR_DEV;

      if (options.outDir) {
        if (typeof options.outDir === "string") {
          outDir = options.outDir;
        } else {
          outDir = isProduction ? options.outDir.prod : options.outDir.dev;
        }
      }

      const manifestPath = options.manifestPath || DEFAULT_MANIFEST_PATH;

      const manifest = yaml.load(
        fs.readFileSync(manifestPath, "utf8")
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
