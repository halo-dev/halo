import { Plugin } from "vite";
import { DEFAULT_OUT_DIR_DEV } from "./constants/build";
import { EXTERNALS, GLOBALS } from "./constants/externals";
import { DEFAULT_MANIFEST_PATH } from "./constants/halo-plugin";
import { getHaloPluginManifest } from "./utils/halo-plugin";

const LEGACY_OUT_DIR_PROD = "../src/main/resources/console";

interface HaloUIPluginBundlerKitOptions {
  outDir?:
    | string
    | {
        dev: string;
        prod: string;
      };
  manifestPath?: string;
}

/**
 * @deprecated Use `viteConfig` or `rsbuildConfig` instead.
 */
export function HaloUIPluginBundlerKit(
  options: HaloUIPluginBundlerKitOptions = {}
): Plugin {
  return {
    name: "halo-ui-plugin-bundler-kit",
    config(config, env) {
      const isProduction = env.mode === "production";

      let outDir = isProduction ? LEGACY_OUT_DIR_PROD : DEFAULT_OUT_DIR_DEV;

      if (options.outDir) {
        if (typeof options.outDir === "string") {
          outDir = options.outDir;
        } else {
          outDir = isProduction ? options.outDir.prod : options.outDir.dev;
        }
      }

      const manifestPath = options.manifestPath || DEFAULT_MANIFEST_PATH;

      const manifest = getHaloPluginManifest(manifestPath);

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
            external: EXTERNALS,
            output: {
              globals: GLOBALS,
              extend: true,
            },
          },
        },
      };
    },
  };
}
