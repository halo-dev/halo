import {
  defineConfig,
  mergeRsbuildConfig,
  type RsbuildConfig,
} from "@rsbuild/core";
import { getHaloPluginManifest } from "./utils/halo-plugin";
import { DEFAULT_OUT_DIR_DEV, DEFAULT_OUT_DIR_PROD } from "./constants/build";
import { pluginVue } from "@rsbuild/plugin-vue";
import { GLOBALS } from "./constants/externals";
import { DEFAULT_MANIFEST_PATH } from "./constants/halo-plugin";

export interface RsBuildUserConfig {
  /**
   * Halo plugin manifest path.
   *
   * @default "../src/main/resources/plugin.yaml"
   */
  manifestPath?: string;

  /**
   * Custom Rsbuild config.
   */
  rsbuild: RsbuildConfig;
}

function createRsbuildPresetsConfig(manifestPath: string) {
  const manifest = getHaloPluginManifest(manifestPath);

  return defineConfig(({ envMode }) => {
    const isProduction = envMode === "production";

    const outDir = isProduction ? DEFAULT_OUT_DIR_PROD : DEFAULT_OUT_DIR_DEV;

    return {
      source: {
        entry: {
          main: "./src/index.ts",
        },
      },
      dev: {
        hmr: false,
      },
      plugins: [pluginVue()],
      tools: {
        rspack: {
          module: {
            parser: {
              javascript: {
                importMeta: false,
              },
            },
          },
          output: {
            publicPath: `/plugins/${manifest.metadata.name}/assets/console/`,
            library: {
              type: "window",
              export: "default",
              name: manifest.metadata.name,
            },
            globalObject: "window",
            iife: true,
          },
        },
        htmlPlugin: false,
      },
      output: {
        distPath: {
          root: outDir,
          js: "",
          css: "",
          jsAsync: "chunks",
          cssAsync: "chunks",
        },
        cleanDistPath: true,
        filename: {
          css: (pathData) => {
            if (pathData.chunk?.name === "main") {
              return "style.css";
            }
            return "[name].[contenthash:8].css";
          },
          js: (pathData) => {
            if (pathData.chunk?.name === "main") {
              return "main.js";
            }
            return "[name].[contenthash:8].js";
          },
        },
        externals: GLOBALS,
      },
    };
  });
}

/**
 * Rsbuild config for Halo UI Plugin.
 *
 * @example
 * ```ts
 * import { rsbuildConfig } from "@halo-dev/ui-plugin-bundler-kit";
 *
 * export default rsbuildConfig({
 *   rsbuild: {
 *     // your custom rsbuild config
 *   },
 * });
 * ```
 * @param config
 * @returns
 */
export function rsbuildConfig(config: RsBuildUserConfig) {
  const presetsConfigFn = createRsbuildPresetsConfig(
    config.manifestPath || DEFAULT_MANIFEST_PATH
  );
  return defineConfig((env) => {
    const presetsConfig = presetsConfigFn(env);
    return mergeRsbuildConfig(presetsConfig, config.rsbuild);
  });
}
