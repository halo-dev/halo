import Vue from "@vitejs/plugin-vue";
import {
  defineConfig,
  mergeConfig,
  UserConfig,
  UserConfigFnObject,
} from "vite";
import { DEFAULT_OUT_DIR_DEV, DEFAULT_OUT_DIR_PROD } from "./constants/build";
import { EXTERNALS, GLOBALS } from "./constants/externals";
import { DEFAULT_MANIFEST_PATH } from "./constants/halo-plugin";
import { getHaloPluginManifest } from "./utils/halo-plugin";

export interface ViteUserConfig {
  /**
   * Halo plugin manifest path.
   *
   * @default "../src/main/resources/plugin.yaml"
   */
  manifestPath?: string;

  /**
   * Custom Vite config.
   */
  vite: UserConfig | UserConfigFnObject;
}

function createVitePresetsConfig(manifestPath: string) {
  const manifest = getHaloPluginManifest(manifestPath);

  return defineConfig(({ mode }) => {
    const isProduction = mode === "production";

    return {
      mode: mode || "production",
      plugins: [Vue()],
      define: { "process.env.NODE_ENV": "'production'" },
      build: {
        outDir: isProduction ? DEFAULT_OUT_DIR_PROD : DEFAULT_OUT_DIR_DEV,
        emptyOutDir: true,
        lib: {
          entry: "src/index.ts",
          name: manifest.metadata.name,
          formats: ["iife"],
          fileName: () => "main.js",
          cssFileName: "style",
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
  });
}

/**
 * Vite config for Halo UI Plugin.
 *
 * @example
 * ```ts
 * import { viteConfig } from "@halo-dev/ui-plugin-bundler-kit";
 *
 * export default viteConfig({
 *   vite: {
 *     // your custom vite config
 *   },
 * });
 * ```
 */
export function viteConfig(config?: ViteUserConfig) {
  const presetsConfigFn = createVitePresetsConfig(
    config?.manifestPath || DEFAULT_MANIFEST_PATH
  );
  return defineConfig((env) => {
    const presetsConfig = presetsConfigFn(env);
    const userConfig =
      typeof config?.vite === "function"
        ? config.vite(env)
        : config?.vite || {};
    return mergeConfig(presetsConfig, userConfig);
  });
}
